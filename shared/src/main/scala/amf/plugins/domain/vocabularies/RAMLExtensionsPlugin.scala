package amf.plugins.domain.vocabularies

import amf.compiler.Root
import amf.document.BaseUnit
import amf.framework.plugins.AMFDomainPlugin
import amf.spec.ParserContext
import amf.spec.dialects.DialectParser
import org.yaml.model.YComment

object DialectHeader {
  def apply(root: Root): Boolean = root.parsed.comment match {
    case Some(comment: YComment) => comment.metaText match {
      case t if t.startsWith("%RAML 1.0 Vocabulary") => true
      case t if t.startsWith("%RAML 1.0 Dialect")    => true
      case t if t.startsWith("%RAML 1.0")            => false
      case t if t.startsWith("%RAML 0.8")            => false
      case t if t.startsWith("%")                    => true
      case _                                         => false
    }
  }
}
class RAMLExtensionsPlugin(dialectsRegistry: amf.dialects.DialectRegistry) extends AMFDomainPlugin {

  override def parse(root: Root, parentContext: ParserContext): Option[BaseUnit] = {
    implicit val ctx: ParserContext = ParserContext(parentContext.validation, parentContext.refs)
    root.parsed.comment match {
      case Some(comment: YComment) =>
        val header = comment.metaText
        if (dialectsRegistry.knowsHeader(header)) {
          Some(DialectParser(root, header, dialectsRegistry).parseUnit())
        } else {
          None
        }
      case _ => None
    }
  }



  def accept(root: Root): Boolean = DialectHeader(root)

  override def domainSyntaxes = Seq(
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml"
  )
}
