package amf.plugins.domain.vocabularies

import amf.client.GenerationOptions
import amf.core.Root
import amf.document.Fragment.DialectFragment
import amf.document._
import amf.domain.dialects.DomainEntity
import amf.framework.plugins.AMFDomainPlugin
import amf.remote.Platform
import amf.spec.ParserContext
import amf.spec.dialects.{DialectEmitter, DialectParser}
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
class RAMLExtensionsPlugin extends AMFDomainPlugin with RamlHeaderExtractor {

  override val ID = "RAML Extension"

  val vendors = Seq("RAML Extension", "RAML 1.0")

  override def parse(root: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    implicit val ctx: ParserContext = ParserContext(parentContext.validation, parentContext.refs)
    comment(root) match {
      case Some(comment: YComment) =>
        val header = comment.metaText
        if (platform.dialectsRegistry.knowsHeader(header)) {
          Some(DialectParser(root, header, platform.dialectsRegistry).parseUnit())
        } else {
          None
        }
      case _ => None
    }
  }

  override def canUnparse(unit: BaseUnit) = unit match {
    case document: Document => document.encodes.isInstanceOf[DomainEntity]
    case module: Module =>
      module.declares exists {
        case _: DomainEntity => true
        case _               => false
      }
    case _: DialectFragment => true
    case _                  => false
  }

  def canParse(root: Root): Boolean = DialectHeader(root)

  override def unparse(unit: BaseUnit, options: GenerationOptions) = Some(DialectEmitter(unit).emit())

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
