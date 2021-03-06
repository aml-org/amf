package amf.plugins.document.webapi.parser.spec.common

import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.DomainElementModel.CustomDomainProperties
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.model.domain.{AmfArray, AmfObject}
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser.parseExtensions
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.resolveAnnotation
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.validations.ParserSideValidations.InvalidAnnotationTarget
import org.yaml.model._

case class AnnotationParser(element: AmfObject, map: YMap, target: List[String] = Nil)(implicit val ctx: WebApiContext) {
  def parse(): Unit = {
    val extensions = parseExtensions(element.id, map, target)
    setExtensions(extensions)
  }

  // NOTE: DONE LIKE THIS BECAUSE OF SCALA JS LINKING ERRORS
  private def customDomainPropertiesFrom(obj: AmfObject) =
    Option(obj.fields.field(CustomDomainProperties)).getOrElse(Seq[DomainExtension]())

  def parseOrphanNode(orphanNodeName: String): Unit = {
    map.key(orphanNodeName) match {
      case Some(orphanMapEntry) if orphanMapEntry.value.tagType == YType.Map =>
        val extensions = parseExtensions(element.id, orphanMapEntry.value.as[YMap])
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
      element.set(DomainElementModel.CustomDomainProperties,
                  AmfArray(oldExtensions ++ extensions, Annotations.inferred()),
                  Annotations.inferred())
  }
}

object AnnotationParser {
  def parseExtensions(parent: String, map: YMap, target: List[String] = Nil)(
      implicit ctx: WebApiContext): Seq[DomainExtension] =
    map.entries.flatMap { entry =>
      resolveAnnotation(entryKey(entry)).map(ExtensionParser(_, parent, entry, target).parse().add(Annotations(entry)))
    }

  private def entryKey(entry: YMapEntry) = {
    entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
  }
}

private case class ExtensionParser(annotation: String, parent: String, entry: YMapEntry, target: List[String] = Nil)(
    implicit val ctx: WebApiContext) {
  def parse(): DomainExtension = {
    val id              = s"$parent/extension/$annotation"
    val propertyId      = s"$parent/$annotation"
    val domainExtension = DomainExtension(Annotations(entry)).withId(id)
    val dataNode        = DataNodeParser(entry.value, parent = Some(propertyId)).parse()
    // TODO
    // throw a parser-side warning validation error if no annotation can be found
    val customDomainProperty = ctx.declarations
      .findAnnotation(annotation, SearchScope.All)
      .getOrElse(
        CustomDomainProperty(Annotations(entry)).withId(propertyId).withName(annotation, Annotations(entry.key)))
    validateAllowedTargets(customDomainProperty)
    domainExtension.adopted(parent)
    domainExtension
      .set(DomainExtensionModel.Extension, dataNode, Annotations.inferred())
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
          ctx.eh.violation(
            InvalidAnnotationTarget,
            parent,
            s"Annotation $annotation not allowed in target ${ramlTarget
              .getOrElse("")}, allowed targets: ${ramlAllowedTargets.mkString(", ")}",
            entry
          )
        }
      case _ =>
    }
  }

}
