package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.errorhandling.AMFErrorHandler
import amf.core.exception.UnsupportedParsedDocumentException
import amf.core.metamodel.domain.ExternalSourceElementModel
import amf.core.model.document.Fragment
import amf.core.parser.{Annotations, SyamlParsedDocument}
import amf.plugins.document.webapi.parser.ShapeParserContext
import amf.plugins.document.webapi.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaVersion, OasTypeParser}
import amf.plugins.document.webapi.parser.spec.jsonschema.AstFinder.getPointedAstOrNode
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaRootCreator.createRootFrom
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ShapeParserSideValidations.UnableToParseJsonSchema

class JsonSchemaParser {

  def parse(inputFragment: Fragment, pointer: Option[String])(implicit ctx: ShapeParserContext): Option[AnyShape] = {

    val doc: Root     = createRootFrom(inputFragment, pointer, ctx.eh)
    val parsingResult = parse(doc, ctx, new ParsingOptions())
    Some(parsingResult)
  }

  def parse(document: Root,
            parentContext: ShapeParserContext,
            options: ParsingOptions,
            optionalVersion: Option[JSONSchemaVersion] = None): AnyShape = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String                  = deriveShapeIdFrom(document)
        val JsonReference(url, hashFragment) = JsonReference.buildReference(document.location)
        val jsonSchemaContext                = makeJsonSchemaContext(document, parentContext, url, options)
        val rootAst                          = getPointedAstOrNode(parsedDoc.document.node, shapeId, hashFragment, url, jsonSchemaContext)
        val version                          = optionalVersion.getOrElse(jsonSchemaContext.computeJsonSchemaVersion(parsedDoc.document.node))
        OasTypeParser(rootAst,
                      keyValueOrDefault(rootAst)(jsonSchemaContext.eh),
                      shape => shape.withId(shapeId),
                      version = version)(jsonSchemaContext)
          .parse() match {
          case Some(shape) => shape
          case None =>
            throwUnparsableJsonSchemaError(document, shapeId, jsonSchemaContext, rootAst)
            SchemaShape()
              .withId(shapeId)
              .set(ExternalSourceElementModel.Raw, document.raw, Annotations.synthesized())
              .set(SchemaShapeModel.MediaType, "application/json", Annotations.synthesized())
        }

      case _ => throw UnsupportedParsedDocumentException
    }
  }

  private def keyValueOrDefault(rootAst: YMapEntryLike)(implicit errorHandler: AMFErrorHandler) = {
    rootAst.key.map(_.as[String]).getOrElse("schema")
  }

  private def makeJsonSchemaContext(document: Root,
                                    parentContext: ShapeParserContext,
                                    url: String,
                                    options: ParsingOptions): ShapeParserContext = {
    parentContext.makeJsonSchemaContextForParsing(url, document, options)
  }

  private def deriveShapeIdFrom(doc: Root): String =
    if (doc.location.contains("#")) doc.location else doc.location + "#/"

  private def throwUnparsableJsonSchemaError(document: Root,
                                             shapeId: String,
                                             jsonSchemaContext: ShapeParserContext,
                                             rootAst: YMapEntryLike): Unit = {
    jsonSchemaContext.eh.violation(UnableToParseJsonSchema,
                                   shapeId,
                                   s"Cannot parse JSON Schema at ${document.location}",
                                   rootAst.value)
  }
}
