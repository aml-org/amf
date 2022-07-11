package amf.shapes.internal.spec.jsonschema.parser

import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.{JsonSchema, Spec}
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser._
import amf.shapes.internal.spec.oas.parser.{OasLikeIgnoreCriteria, OasLink}
import org.yaml.model.YNode

case class JsonSchemaSettings(syntax: SpecSyntax, defaultSchemaVersion: SchemaVersion) extends SpecSettings {
  override val spec: Spec = JsonSchema

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType]               = None
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: CommonShapeParseContext if ctx.isOas2Context || ctx.isOas3Context => true
    case ctx: CommonShapeParseContext if ctx.isRamlContext                      => false
    case _                                                                      => false
  }
}
