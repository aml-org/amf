package amf.apicontract.internal.spec.avro

import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec.AVRO_SCHEMA
import amf.shapes.internal.spec.common.parser._
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, SchemaPosition, SchemaVersion}
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.RamlWebApiContextType
import org.yaml.model.YNode

object AvroSettings extends SpecSettings {
  override val spec: Spec = AVRO_SCHEMA

  override def link(node: YNode)(implicit eh: SyamlAMFErrorHandler): Either[String, YNode] = Left(node.toString)

  override def ignoreCriteria: IgnoreCriteria = IgnoreAllCriteria

  override def ramlContextType: Option[RamlWebApiContextType] = None

  override val defaultSchemaVersion: SchemaVersion = OAS20SchemaVersion.apply(SchemaPosition.Other)
  override val annotationValidatorBuilder: AnnotationSchemaValidatorBuilder = IgnoreAnnotationSchemaValidatorBuilder

  override def shouldLinkTypes(parent: ParserContext): Boolean = parent match {
    case ctx: ShapeParserContext if ctx.isRamlContext => false
    case _                                            => true
  }

  override val syntax: SpecSyntax = SpecSyntax.empty
}
