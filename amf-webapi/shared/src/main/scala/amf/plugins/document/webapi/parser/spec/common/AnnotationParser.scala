package amf.plugins.document.webapi.parser.spec.common

import amf.core.model.domain.DomainElement
import amf.core.model.domain.extensions.{BaseDomainExtension, CustomDomainProperty, DomainExtension}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser.parseExtensions
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.resolveAnnotation
import org.yaml.model._

case class AnnotationParser(element: DomainElement, map: YMap)(implicit val ctx: WebApiContext) {
  def parse(): Unit = {
    val extensions = parseExtensions(element.id, map)
    if (extensions.nonEmpty) element.withCustomDomainProperties(extensions)
  }
}

object AnnotationParser {
  def parseExtensions(parent: String, map: YMap)(implicit ctx: WebApiContext): Seq[BaseDomainExtension] =
    map.entries.flatMap { entry =>
      val key = entry.key.as[YScalar].text
      resolveAnnotation(key).map(ExtensionParser(_, parent, entry).parse().add(Annotations(entry)))
    }
}

private case class ExtensionParser(annotation: String, parent: String, entry: YMapEntry)(
    implicit val ctx: WebApiContext) {
  def parse(): BaseDomainExtension = {
    val domainExtension = DomainExtension()
    val dataNode        = DataNodeParser(entry.value, parent = Some(s"$parent/$annotation")).parse()
    // TODO
    // throw a parser-side warning validation error if no annotation can be found
    val customDomainProperty = ctx.declarations.annotations
      .getOrElse(annotation, CustomDomainProperty(Annotations(entry)).withName(annotation))
    domainExtension.adopted(parent)
    domainExtension
      .withExtension(dataNode)
      .withDefinedBy(customDomainProperty)
      .withName(annotation)
  }
}
