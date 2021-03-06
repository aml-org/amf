package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.exception.UnsupportedParsedDocumentException
import amf.core.metamodel.domain.ExternalSourceElementModel
import amf.core.model.document.{EncodesModel, Fragment}
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{Annotations, EmptyFutureDeclarations, ParserContext, SyamlParsedDocument}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.Raml08WebApiContext
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaVersion, OasTypeParser}
import amf.plugins.document.webapi.parser.spec.jsonschema.AstFinder.getPointedAstOrNode
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaRootCreator.createRootFrom
import amf.plugins.document.webapi.parser.spec.toOasDeclarations
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ParserSideValidations.UnableToParseJsonSchema

class JsonSchemaParser {

  def parse(inputFragment: Fragment, pointer: Option[String])(implicit ctx: OasLikeWebApiContext): Option[AnyShape] = {

    val doc: Root     = createRootFrom(inputFragment, pointer, ctx.eh)
    val parsingResult = parse(doc, ctx, new ParsingOptions())

    parsingResult match {
      case encoded: EncodesModel if encoded.encodes.isInstanceOf[AnyShape] =>
        Some(encoded.encodes.asInstanceOf[AnyShape])
      case _ => None
    }
  }

  def parse(document: Root,
            parentContext: WebApiContext,
            options: ParsingOptions,
            optionalVersion: Option[JSONSchemaVersion] = None): DataTypeFragment = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String                  = deriveShapeIdFrom(document)
        val JsonReference(url, hashFragment) = JsonReference.buildReference(document.location)
        val jsonSchemaContext                = makeJsonSchemaContext(document, parentContext, url, options)
        val rootAst                          = getPointedAstOrNode(parsedDoc.document.node, shapeId, hashFragment, url, jsonSchemaContext)
        val version                          = optionalVersion.getOrElse(jsonSchemaContext.computeJsonSchemaVersion(parsedDoc.document.node))
        val parsed =
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
        val unit = wrapInDataTypeFragment(document, parsed)
        unit
      case _ => throw UnsupportedParsedDocumentException
    }
  }

  private def keyValueOrDefault(rootAst: YMapEntryLike)(implicit errorHandler: ParserErrorHandler) = {
    rootAst.key.map(_.as[String]).getOrElse("schema")
  }

  private def makeJsonSchemaContext(document: Root,
                                    parentContext: WebApiContext,
                                    url: String,
                                    options: ParsingOptions): JsonSchemaWebApiContext = {

    val cleanNested = ParserContext(url, document.references, EmptyFutureDeclarations(), parentContext.eh)
    cleanNested.globalSpace = parentContext.globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations = getInheritedDeclarations(parentContext)

    val schemaContext = new JsonSchemaWebApiContext(url,
                                                    document.references,
                                                    cleanNested,
                                                    inheritedDeclarations,
                                                    options,
                                                    parentContext.defaultSchemaVersion)
    schemaContext.indexCache = parentContext.indexCache
    schemaContext
  }

  private def getInheritedDeclarations(parserContext: ParserContext) = {
    parserContext match {
      case ramlContext: Raml08WebApiContext => Some(toOasDeclarations(ramlContext.declarations))
      case _                                => None
    }
  }

  private def deriveShapeIdFrom(doc: Root): String =
    if (doc.location.contains("#")) doc.location else doc.location + "#/"

  private def throwUnparsableJsonSchemaError(document: Root,
                                             shapeId: String,
                                             jsonSchemaContext: JsonSchemaWebApiContext,
                                             rootAst: YMapEntryLike): Unit = {
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
