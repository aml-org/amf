package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.model.document.{BaseUnit, EncodesModel, Fragment}
import amf.core.parser.{ParserContext, SyamlParsedDocument}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ParserSideValidations.UnableToParseJsonSchema

class JsonSchemaParser {

  def parse(inputFragment: Fragment, pointer: Option[String])(
    implicit ctx: OasLikeWebApiContext): Option[AnyShape] = {

    val doc: Root = AstFinder.createRootFrom(inputFragment, pointer, ctx.eh)
    val parsingResult = parse(doc, ctx, new ParsingOptions())

    parsingResult.collect {
      case encoded: EncodesModel if encoded.encodes.isInstanceOf[AnyShape] => encoded.encodes.asInstanceOf[AnyShape]
    }
  }

  def parse(document: Root, parentContext: ParserContext, options: ParsingOptions): Option[BaseUnit] = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String      = AstFinder.deriveShapeIdFrom(document)
        val JsonReference(url, hashFragment) = JsonReference.buildReference(document.location, JsonSchemaUrlFragmentAdapter)
        val jsonSchemaContext = AstFinder.makeJsonSchemaContext(document, parentContext, url, options)
        val rootAst = AstFinder.getRootAst(parsedDoc, shapeId, hashFragment, url, jsonSchemaContext)
        val version = jsonSchemaContext.computeJsonSchemaVersion(rootAst.value)
        val parsed =
          OasTypeParser(rootAst, rootAst.key.map(_.as[String]).getOrElse("schema"), shape => shape.withId(shapeId), version = version)(jsonSchemaContext)
            .parse() match {
            case Some(shape) => shape
            case None =>
              throwUnparsableJsonSchemaError(document, shapeId, jsonSchemaContext, rootAst)
              SchemaShape().withId(shapeId).withMediaType("application/json").withRaw(document.raw)
          }
        val unit = wrapInDataTypeFragment(document, parsed)
        Some(unit)
      case _ => None
    }
  }

  private def throwUnparsableJsonSchemaError(document: Root, shapeId: String, jsonSchemaContext: JsonSchemaWebApiContext, rootAst: YMapEntryLike): Unit = {
    jsonSchemaContext.eh.violation(UnableToParseJsonSchema,
      shapeId,
      s"Cannot parse JSON Schema at ${document.location}",
      rootAst.value)
  }

  private def wrapInDataTypeFragment(document: Root, parsed: AnyShape): DataTypeFragment = {
    val unit: DataTypeFragment =
      DataTypeFragment().withId(document.location).withLocation(document.location).withEncodes(parsed)
    unit.withRaw(document.raw)
    unit
  }
}
