package amf.apicontract.internal.spec.raml.parser.external

import amf.apicontract.internal.spec.common.parser.{WebApiContext, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.jsonschema.JsonSchemaWebApiContext
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.external.RamlJsonSchemaExpression.{errorShape, withScopedContext}
import amf.apicontract.internal.spec.raml.parser.external.SharedStuff.toSchemaContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.JsonSchemaFragmentNotFound
import amf.core.client.scala.parse.document._
import amf.core.internal.annotations.ExternalFragmentRef
import amf.core.internal.parser.domain.JsonParserFactory
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.UriUtils
import amf.shapes.client.scala.model.domain.{AnyShape, SchemaShape, UnresolvedShape}
import amf.shapes.internal.annotations._
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.ExternalFragmentHelper
import amf.shapes.internal.spec.common.parser.ExternalFragmentHelper.searchForAlreadyParsedNodeInFragments
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaParsingHelper
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser.external.{RamlExternalTypesParser, ValueAndOrigin}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseJsonSchema
import org.mulesoft.lexer.Position
import org.yaml.model._
import org.yaml.parser.JsonParser

object RamlJsonSchemaExpression {
  def withScopedContext[T](valueAST: YNode, schemaEntry: YMapEntry)(
      block: JsonSchemaWebApiContext => T
  )(implicit ctx: WebApiContext): T = {
    val nextContext = getContext(valueAST, schemaEntry)
    val parsed      = block(nextContext)
    cleanGlobalSpace(nextContext)            // this works because globalSpace is mutable everywhere
    nextContext.removeLocalJsonSchemaContext // we reset the JSON schema context after parsing
    parsed
  }

  private def getContext(valueAST: YNode, schemaEntry: YMapEntry)(implicit ctx: WebApiContext) = {
    // we set the local schema entry to be able to resolve local $refs
    ctx.setJsonSchemaAST(schemaEntry.value)
    toSchemaContext(ctx, valueAST)
  }

  /** Clean from globalSpace the local references
    */
  private def cleanGlobalSpace(ctx: WebApiContext): Unit = {
    ctx.globalSpace.foreach { e =>
      val refPath = e._1.split("#").headOption.getOrElse("")
      if (refPath == ctx.getLocalJsonSchemaContext.get.sourceName) ctx.globalSpace.remove(e._1)
    }
  }

  def errorShape(value: YNode)(implicit ctx: WebApiContext) = {
    val shape = SchemaShape()
    ctx.eh.violation(UnableToParseJsonSchema, shape, "Cannot parse JSON Schema", value.location)
    shape
  }
}

case class RamlJsonSchemaExpression(
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
      parseSchema(origin)

  }

  private def parseSchema(origin: ValueAndOrigin) = {
    val parsed = parseJsonSchema(origin)
    parsed.annotations += SchemaIsJsonSchema()
    parsed
  }

  private def parseWrappedSchema(origin: ValueAndOrigin, map: YMap): AnyShape = {
    val parsed: AnyShape  = parseWrappedSchema(origin)
    val wrapper: AnyShape = parseSchemaWrapper(map, parsed)
    wrapper
  }

  private def parseSchemaWrapper(map: YMap, parsed: AnyShape) =
    SchemaWrapperParser.parse(map, parsed, key, value)(shapeCtx)

  private def parseWrappedSchema(origin: ValueAndOrigin): AnyShape = {
    val parsed = parseJsonSchema(origin)
    parsed.annotations += SchemaIsJsonSchema()
    parsed.withName("schema")
    parsed
  }

  private def parseJsonSchema(origin: ValueAndOrigin) = {
    origin.originalUrlText match {
      case Some(url) =>
        IncludedJsonSchemaParser(key, value).parse(origin, url).add(ExternalReferenceUrl(url))
      case None =>
        InlineJsonSchemaParser.parse(key, value, origin)
    }
  }

  override val externalType: String = "JSON"
}
