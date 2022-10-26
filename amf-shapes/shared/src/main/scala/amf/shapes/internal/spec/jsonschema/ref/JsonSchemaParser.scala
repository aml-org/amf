package amf.shapes.internal.spec.jsonschema.ref

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.exception.UnsupportedParsedDocumentException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.domain.ExternalSourceElementModel
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Mimes.`application/json`
import amf.shapes.client.scala.model.domain.{AnyShape, SchemaShape}
import amf.shapes.internal.domain.metamodel.SchemaShapeModel
import amf.shapes.internal.spec.common.JSONSchemaVersion
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, YMapEntryLike}
import amf.shapes.internal.spec.jsonschema.ref.AstFinder.getPointedAstOrNode
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaRootCreator.createRootFrom
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseJsonSchema

class JsonSchemaParser {

  def parse(inputFragment: BaseUnit, pointer: Option[String])(implicit ctx: ShapeParserContext): Option[AnyShape] = {

    val doc: Root     = createRootFrom(inputFragment, pointer, ctx)
    val parsingResult = parse(doc, ctx, ParsingOptions())
    Some(parsingResult)
  }

  def parse(
      document: Root,
      parentContext: ShapeParserContext,
      options: ParsingOptions,
      optionalVersion: Option[JSONSchemaVersion] = None
  ): AnyShape = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String                  = deriveShapeIdFrom(document)
        val JsonReference(url, hashFragment) = JsonReference.buildReference(document.location)
        val jsonSchemaContext                = makeJsonSchemaContext(document, parentContext, url, options)
        val rootAst = getPointedAstOrNode(parsedDoc.document.node, shapeId, hashFragment, url, jsonSchemaContext)
        val version = optionalVersion.getOrElse(jsonSchemaContext.computeJsonSchemaVersion(parsedDoc.document.node))
        OasTypeParser(
          rootAst,
          keyValueOrDefault(rootAst)(jsonSchemaContext.eh),
          shape => shape.withId(shapeId),
          version = version
        )(jsonSchemaContext)
          .parse() match {
          case Some(shape) => shape
          case None =>
            throwUnparsableJsonSchemaError(document, shapeId, jsonSchemaContext, rootAst)
            SchemaShape()
              .withId(shapeId)
              .set(ExternalSourceElementModel.Raw, document.raw, Annotations.synthesized())
              .set(SchemaShapeModel.MediaType, `application/json`, Annotations.synthesized())
        }

      // TODO: check
      case _ => throw UnsupportedParsedDocumentException
    }
  }

  private def keyValueOrDefault(rootAst: YMapEntryLike)(implicit errorHandler: AMFErrorHandler) = {
    rootAst.key.map(_.as[String]).getOrElse("schema")
  }

  private def makeJsonSchemaContext(
      document: Root,
      parentContext: ShapeParserContext,
      url: String,
      options: ParsingOptions
  ): ShapeParserContext = {
    parentContext.makeJsonSchemaContextForParsing(url, document, options)
  }

  private def deriveShapeIdFrom(doc: Root): String =
    if (doc.location.contains("#")) doc.location else doc.location + "#/"

  private def throwUnparsableJsonSchemaError(
      document: Root,
      shapeId: String,
      jsonSchemaContext: ShapeParserContext,
      rootAst: YMapEntryLike
  ): Unit = {
    jsonSchemaContext.eh.violation(
      UnableToParseJsonSchema,
      shapeId,
      s"Cannot parse JSON Schema at ${document.location}",
      rootAst.value.location
    )
  }
}
