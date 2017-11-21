package amf.plugins.domain.vocabularies

import amf.core.Root
import amf.document.BaseUnit
import amf.framework.plugins.AMFDomainPlugin
import amf.spec.ParserContext
import amf.spec.dialects.DialectParser
import org.yaml.model.{YComment, YDocument}

trait RamlHeaderExtractor {
  def comment(root: Root): Option[YComment] = root.parsed.comment

  def comment(document: YDocument): Option[YComment] =
    document.children.find(v => v.isInstanceOf[YComment]).asInstanceOf[Option[YComment]]
}

object DialectHeader extends RamlHeaderExtractor {
  def apply(root: Root): Boolean = comment(root) match {
    case Some(comment: YComment) => comment.metaText match {
      case t if t.startsWith("%RAML 1.0 Vocabulary") => true
      case t if t.startsWith("%RAML 1.0 Dialect")    => true
      case t if t.startsWith("%RAML 1.0")            => false
      case t if t.startsWith("%RAML 0.8")            => false
      case t if t.startsWith("%")                    => true
      case _                                         => false
    }
    case _                                           => false
  }
}
class RAMLExtensionsPlugin(dialectsRegistry: amf.dialects.DialectRegistry) extends AMFDomainPlugin with RamlHeaderExtractor {

  override val ID = "RAML 1.0"

  override def parse(root: Root, parentContext: ParserContext): Option[BaseUnit] = {
    implicit val ctx: ParserContext = ParserContext(parentContext.validation, parentContext.refs)
    comment(root) match {
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

  override def referenceCollector() = new RAMLExtensionsReferenceCollector()
}
