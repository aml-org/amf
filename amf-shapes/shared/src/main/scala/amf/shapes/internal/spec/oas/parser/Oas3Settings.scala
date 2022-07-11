package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec.OAS30
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser._
import amf.shapes.internal.spec.common.{OAS30SchemaVersion, SchemaPosition, SchemaVersion}
import org.yaml.model.YNode

case class Oas3Settings(syntax: SpecSyntax) extends SpecSettings {
  override val spec: Spec = OAS30

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = OasLink.getLinkValue(node)

  override def ignoreCriteria: IgnoreCriteria = OasLikeIgnoreCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override val defaultSchemaVersion: SchemaVersion = OAS30SchemaVersion.apply(SchemaPosition.Other)
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext) = parent match {
    case ctx: CommonShapeParseContext if ctx.isRamlContext => false
    case _                                                 => true
  }
}
