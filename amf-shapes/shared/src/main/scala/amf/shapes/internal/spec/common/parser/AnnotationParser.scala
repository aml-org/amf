package amf.shapes.internal.spec.common.parser

import amf.aml.internal.semantic.{SemanticExtensionsFacade, SemanticExtensionsFacadeBuilder}
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.client.scala.model.domain.{AmfArray, AmfObject}
import amf.core.client.scala.parse.document.{ErrorHandlingContext, ParserContext}
import amf.core.internal.datanode.{DataNodeParser, DataNodeParserContext}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.domain.DomainElementModel.CustomDomainProperties
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel
import amf.core.internal.parser.domain._
import amf.core.internal.parser.{LimitedParseConfig, YMapOps}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.OrphanOasExtension
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.AnnotationParser.parseExtensions
import amf.shapes.internal.spec.common.parser.WellKnownAnnotation.resolveAnnotation
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidAnnotationTarget
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.yaml.model._

case class AnnotationParser(element: AmfObject, map: YMap, target: List[String] = Nil)(implicit
    val ctx: ShapeParserContext
) {
  def parse(): Unit = {

    val extensions = parseExtensions(Some(element), map, target, Some(ctx.extensionsFacadeBuilder))
    setExtensions(extensions)
  }

  // NOTE: DONE LIKE THIS BECAUSE OF SCALA JS LINKING ERRORS
  private def customDomainPropertiesFrom(obj: AmfObject): Seq[DomainExtension] =
    Option(obj.fields.field(CustomDomainProperties)).getOrElse(Seq[DomainExtension]())

  def parseOrphanNode(orphanNodeName: String): Unit = {
    map.key(orphanNodeName) match {
      case Some(orphanMapEntry) if orphanMapEntry.value.tagType == YType.Map =>
        val extensions = parseExtensions(Some(element), orphanMapEntry.value.as[YMap])
        extensions.foreach { extension =>
          Option(extension.extension).foreach(_.annotations += OrphanOasExtension(orphanNodeName))
        }
        if (extensions.nonEmpty) setExtensions(extensions)
      case _ => // ignore
    }
  }

  private def setExtensions(extensions: Seq[DomainExtension]): Unit = {
    val oldExtensions = customDomainPropertiesFrom(element)
    if (extensions.nonEmpty)
      element.setWithoutId(
        DomainElementModel.CustomDomainProperties,
        AmfArray(oldExtensions ++ extensions, Annotations.inferred()),
        Annotations.inferred()
      )
  }
}

object AnnotationParser {
  def parseExtensions(
      parent: Option[AmfObject],
      map: YMap,
      target: List[String] = Nil,
      semanticParserBuilder: Option[SemanticExtensionsFacadeBuilder] = None
  )(implicit ctx: ErrorHandlingContext with DataNodeParserContext with IllegalTypeHandler): Seq[DomainExtension] =
    map.entries.flatMap { entry =>
      resolveAnnotation(entryKey(entry)).map { annotation =>
        val elementTypes = parent.map(_.meta.`type`.map(_.iri())).getOrElse(List.empty)
        parseSemantic(entry, elementTypes, semanticParserBuilder.map(_.extensionName(annotation)))
          .getOrElse(ExtensionParser(annotation, parent, entry, target).parse().add(Annotations(entry)))
      }
    }

  private def parseSemantic(
      entry: YMapEntry,
      elementTypes: Seq[String],
      semanticParser: Option[SemanticExtensionsFacade]
  )(implicit ctx: ErrorHandlingContext): Option[DomainExtension] = {
    semanticParser.flatMap { parser =>
      val nextCtx        = ParserContext(config = LimitedParseConfig(ctx.eh, parser.registry))
      val maybeExtension = parser.parse(elementTypes, entry, nextCtx, "nonImportantId")
      // Inject and anyShape inside the SemEx to avoid validation of annotationType definition
      maybeExtension.foreach(_.definedBy.withSchema(AnyShape()))
      maybeExtension
    }
  }

  private def entryKey(entry: YMapEntry): String = {
    entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
  }
}

private case class ExtensionParser(
    annotation: String,
    parent: Option[AmfObject],
    entry: YMapEntry,
    target: List[String] = Nil
)(implicit val ctx: ErrorHandlingContext with DataNodeParserContext with IllegalTypeHandler) {
  def parse(): DomainExtension = {
    val domainExtension = DomainExtension(Annotations(entry))
    val dataNode        = DataNodeParser(entry.value).parse()
    // TODO
    // throw a parser-side warning validation error if no annotation can be found
    val customDomainProperty = ctx
      .findAnnotation(annotation, SearchScope.All)
      .getOrElse(CustomDomainProperty(Annotations(entry)).withName(annotation, Annotations(entry.key)))
    validateAllowedTargets(customDomainProperty)
    domainExtension
      .setWithoutId(DomainExtensionModel.Extension, dataNode, Annotations.inferred())
      .withName(annotation, Annotations(entry.key))
    domainExtension.fields.setWithoutId(DomainExtensionModel.DefinedBy, customDomainProperty, Annotations.inferred())
    domainExtension
  }

  private def validateAllowedTargets(customDomainProperty: CustomDomainProperty): Unit = {
    (Option(customDomainProperty.domain), target) match {
      case (Some(allowedTargets), _) if allowedTargets.nonEmpty && target.nonEmpty =>
        if (allowedTargets.map(_.value()).intersect(target).isEmpty) {
          val ramlTarget         = VocabularyMappings.uriToRaml.get(target.head)
          val ramlAllowedTargets = allowedTargets.flatMap(uri => VocabularyMappings.uriToRaml.get(uri.value()))
          val msg = s"Annotation $annotation not allowed in target ${ramlTarget
              .getOrElse("")}, allowed targets: ${ramlAllowedTargets.mkString(", ")}"
          parent match {
            case Some(obj) => ctx.eh.violation(InvalidAnnotationTarget, obj, msg, entry.location)
            case None      => ctx.eh.violation(InvalidAnnotationTarget, "", msg, entry.location)
          }
        }
      case _ =>
    }
  }

}
