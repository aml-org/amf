package amf.plugins.document.webapi.parser.spec.common

import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.model.domain.DomainElement
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser.parseExtensions
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.resolveAnnotation
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import org.yaml.model._

case class AnnotationParser(element: DomainElement, map: YMap)(implicit val ctx: WebApiContext) {
  def parse(): Unit = {
    val extensions    = parseExtensions(element.id, map)
    val oldExtensions = Option(element.customDomainProperties).getOrElse(Nil)
    if (extensions.nonEmpty) element.withCustomDomainProperties(oldExtensions ++ extensions)
  }

  def parseOrphanNode(orphanNodeName: String): Unit = {
    map.key(orphanNodeName) match {
      case Some(orphanMapEntry) if orphanMapEntry.value.tagType == YType.Map =>
        val extensions = parseExtensions(element.id, orphanMapEntry.value.as[YMap])
        extensions.foreach { extension =>
          Option(extension.extension).foreach(_.annotations += OrphanOasExtension(orphanNodeName))
        }
        val oldExtensions = Option(element.customDomainProperties).getOrElse(Nil)
        if (extensions.nonEmpty) element.withCustomDomainProperties(oldExtensions ++ extensions)
      case _ => // ignore
    }
  }
}

object AnnotationParser {
  def parseExtensions(parent: String, map: YMap)(implicit ctx: WebApiContext): Seq[DomainExtension] =
    map.entries.flatMap { entry =>
      val key = entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
      resolveAnnotation(key).map(ExtensionParser(_, parent, entry).parse().add(Annotations(entry)))
    }
}

private case class ExtensionParser(annotation: String, parent: String, entry: YMapEntry)(
    implicit val ctx: WebApiContext) {
  def parse(): DomainExtension = {
    val id              = s"$parent/extension/$annotation"
    val propertyId      = s"$parent/$annotation"
    val domainExtension = DomainExtension().withId(id)
    val dataNode        = DataNodeParser(entry.value, parent = Some(propertyId)).parse()
    // TODO
    // throw a parser-side warning validation error if no annotation can be found
    val customDomainProperty = ctx.declarations.annotations
      .getOrElse(annotation, CustomDomainProperty(Annotations(entry)).withId(propertyId).withName(annotation))
    domainExtension.adopted(parent)
    domainExtension
      .withExtension(dataNode)
      .withName(annotation)
    domainExtension.fields.setWithoutId(DomainExtensionModel.DefinedBy, customDomainProperty)
    domainExtension
  }
}
