package amf.shapes.internal.spec.async.parser

import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec.ASYNC20
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser._
import amf.shapes.internal.spec.common.{JSONSchemaDraft7SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.oas.parser.{OasLikeIgnoreCriteria, OasLink}
import org.yaml.model.YNode

case class Async2Settings(syntax: SpecSyntax) extends SpecSettings {
  override val spec: Spec = ASYNC20

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override val defaultSchemaVersion: SchemaVersion                          = JSONSchemaDraft7SchemaVersion
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: ShapeParserContext if ctx.isRamlContext => false
    case _                                            => true
  }
}
