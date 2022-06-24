package amf.apicontract.internal.spec.raml.parser.external

import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.external.json.{
  IncludedJsonSchemaParser,
  InlineJsonSchemaParser,
  SchemaWrapperParser
}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations._
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.raml.parser.external.{RamlExternalTypesParser, ValueAndOrigin}
import org.yaml.model._

case class RamlJsonSchemaParser(
    key: YNode,
    override val value: YNode,
    parseExample: Boolean = false
)(implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser
    with PlatformSecrets {

  override val shapeCtx: ShapeParserContext = WebApiShapeParserContextAdapter(ctx)

  override def parseValue(origin: ValueAndOrigin): AnyShape = value.value match {
    case map: YMap if parseExample =>
      parseWrappedSchema(origin, map)
    case _ =>
      parseJsonSchema(origin)

  }

  private def parseWrappedSchema(origin: ValueAndOrigin, map: YMap): AnyShape = {
    val parsed: AnyShape  = parseWrappedSchema(origin)
    val wrapper: AnyShape = parseSchemaWrapper(map, parsed)
    wrapper
  }

  private def parseWrappedSchema(origin: ValueAndOrigin): AnyShape = {
    val parsed = parseJsonSchema(origin)
    parsed.withName("schema")
    parsed
  }

  private def parseSchemaWrapper(map: YMap, parsed: AnyShape) =
    SchemaWrapperParser.parse(map, parsed, key, value)(shapeCtx)

  private def parseJsonSchema(origin: ValueAndOrigin) = {
    val parsed = origin.originalUrlText match {
      case Some(url) =>
        IncludedJsonSchemaParser(key, value).parse(origin, url).add(ExternalReferenceUrl(url))
      case None =>
        InlineJsonSchemaParser.parse(key, value, origin)
    }
    parsed.annotations += SchemaIsJsonSchema()
    parsed
  }

  override val externalType: String = "JSON"
}
