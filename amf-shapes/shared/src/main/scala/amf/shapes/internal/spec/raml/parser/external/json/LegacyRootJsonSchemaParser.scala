package amf.shapes.internal.spec.raml.parser.external.json

import amf.core.internal.utils.UriUtils
import amf.shapes.client.scala.model.domain.{AnyShape, UnresolvedShape}
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.ExternalFragmentHelper.searchForAlreadyParsedNodeInFragments
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaParsingHelper
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import org.yaml.model.{YMapEntry, YNode}

class LegacyRootJsonSchemaParser(key: YNode, ast: YNode)(implicit ctx: ShapeParserContext)
    extends JsonParsing
    with ScopedJsonContext
    with ErrorShapeCreation {

  def parse(origin: ValueAndOrigin, basePath: String): AnyShape = {
    val shape = parseJsonShape(origin.text, key, origin.valueAST, ast, origin.originalUrlText)
    ctx.fragments
      .get(basePath)
      .foreach(e => shape.callAfterAdoption(() => shape.withReference(e.encoded.id)))
    ctx.registerExternalRef(basePath, shape)
    shape.annotations += ParsedJSONSchema(origin.text.trim)
    shape
  }

  private def parseJsonShape(
      text: String,
      key: YNode,
      valueAST: YNode,
      value: YNode,
      extLocation: Option[String]
  ): AnyShape = {

    val node: YNode = parseAst(text, valueAST, extLocation)
    val schemaEntry = YMapEntry(key, node)
    val shape = withScopedContext(valueAST, schemaEntry) { jsonSchemaContext =>
      val fullRef = UriUtils.normalizePath(jsonSchemaContext.rootContextDocument)

      val tmpShape: UnresolvedShape = initializedUnresolved(schemaEntry, jsonSchemaContext, fullRef)

      val parsed = parse(value, schemaEntry, jsonSchemaContext, fullRef, tmpShape)
      propagatePromotedFragments(jsonSchemaContext)
      parsed
    }
    shape
  }

  private def initializedUnresolved(
      schemaEntry: YMapEntry,
      jsonSchemaShapeContext: ShapeParserContext,
      fullRef: String
  ) = {
    JsonSchemaParsingHelper.createTemporaryShape(
      _ => {},
      schemaEntry,
      jsonSchemaShapeContext,
      fullRef
    )
  }

  private def parseAst(text: String, valueAST: YNode, extLocation: Option[String]) = {
    searchForAlreadyParsedNodeInFragments(valueAST).getOrElse {
      getJsonParserFor(text, valueAST, extLocation).document().node
    }
  }

  private def propagatePromotedFragments(jsonSchemaContext: ShapeParserContext): Unit = {
    if (jsonSchemaContext.promotedFragments.nonEmpty) {
      ctx.addPromotedFragments(jsonSchemaContext.promotedFragments)
    }
  }

  private def parse(
      value: YNode,
      schemaEntry: YMapEntry,
      jsonSchemaContext: ShapeParserContext,
      fullRef: String,
      tmpShape: UnresolvedShape
  ) = {
    OasTypeParser(schemaEntry, _ => {}, ctx.computeJsonSchemaVersion(schemaEntry.value))(jsonSchemaContext)
      .parse() match {
      case Some(sh) =>
        ctx.futureDeclarations.resolveRef(fullRef, sh)
        ctx.registerJsonSchema(fullRef, sh)
        tmpShape.resolve(sh) // useless?
        if (sh.isLink) sh.effectiveLinkTarget().asInstanceOf[AnyShape]
        else sh
      case None => errorShape(value)
    }
  }
}
