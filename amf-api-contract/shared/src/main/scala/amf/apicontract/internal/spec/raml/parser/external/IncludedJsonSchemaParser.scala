package amf.apicontract.internal.spec.raml.parser.external

import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.external.RamlJsonSchemaExpression.{errorShape, withScopedContext}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.JsonSchemaFragmentNotFound
import amf.core.client.scala.parse.document.ReferenceFragmentPartition
import amf.core.internal.annotations.ExternalFragmentRef
import amf.core.internal.parser.domain.JsonParserFactory
import amf.core.internal.utils.UriUtils
import amf.shapes.client.scala.model.domain.{AnyShape, UnresolvedShape}
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.ExternalFragmentHelper.searchForAlreadyParsedNodeInFragments
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaParsingHelper
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import org.mulesoft.lexer.Position
import org.yaml.model.{YMapEntry, YNode}
import org.yaml.parser.JsonParser

case class IncludedJsonSchemaParser(key: YNode, ast: YNode)(implicit ctx: RamlWebApiContext) {

  private val shapeCtx: ShapeParserContext = WebApiShapeParserContextAdapter(ctx)

  def parse(origin: ValueAndOrigin, url: String) = {
    val (basePath, localPath) = ReferenceFragmentPartition(url)
    val normalizedLocalPath   = localPath.map(_.stripPrefix("/definitions/"))
    findInExternals(basePath, normalizedLocalPath) match {
      case Some(s) =>
        copyExternalShape(basePath, s, localPath)
      case _ if isInnerSchema(normalizedLocalPath) => // oas lib
        parseOasLib(origin, basePath, localPath, normalizedLocalPath)
      case _ =>
        parseFragment(origin, basePath)
    }
  }

  private def isInnerSchema(normalizedLocalPath: Option[String]) = normalizedLocalPath.isDefined

  private def findInExternals(basePath: String, normalizedLocalPath: Option[String]) = {
    normalizedLocalPath
      .flatMap(ctx.declarations.findInExternalsLibs(basePath, _))
      .orElse(ctx.declarations.findInExternals(basePath))
  }

  private def copyExternalShape(basePath: String, s: AnyShape, localPath: Option[String]) = {
    val shape = s.copyShape().withName(key.as[String])
    ctx.declarations.fragments
      .get(basePath)
      .foreach(e =>
        shape.callAfterAdoption { () =>
          shape.withReference(e.encoded.id + localPath.getOrElse(""))
        }
      )
    if (shape.examples.nonEmpty) { // top level inlined shape, we don't want to reuse the ID, this must be an included JSON schema => EDGE CASE!
      // We remove the examples declared in the previous endpoint for this inlined shape , see previous comment about the edge case
      shape.fields.remove(AnyShapeModel.Examples.value.iri())
    }
    shape
  }

  private def parseOasLib(
      origin: ValueAndOrigin,
      basePath: String,
      localPath: Option[String],
      normalizedLocalPath: Option[String]
  ) = {
    RamlExternalOasLibParser(ctx, origin.text, origin.valueAST, basePath).parse()
    val shape = ctx.declarations.findInExternalsLibs(basePath, normalizedLocalPath.get) match {
      case Some(s) =>
        s.copyShape().withName(key.as[String])
      case _ =>
        val empty = AnyShape()
        ctx.eh.violation(
          JsonSchemaFragmentNotFound,
          empty,
          s"could not find json schema fragment ${localPath.get} in file $basePath",
          origin.valueAST.location
        )
        empty

    }
    ctx.declarations.fragments
      .get(basePath)
      .foreach(e =>
        shape.callAfterAdoption { () =>
          shape.withReference(e.encoded.id + localPath.get)
        }
      )

    shape.annotations += ExternalFragmentRef(localPath.get)
    shape
  }

  private def parseFragment(origin: ValueAndOrigin, basePath: String) = {
    val shape = parseJsonShape(origin.text, key, origin.valueAST, ast, origin.originalUrlText)
    ctx.declarations.fragments
      .get(basePath)
      .foreach(e => shape.callAfterAdoption(() => shape.withReference(e.encoded.id)))
    ctx.declarations.registerExternalRef(basePath, shape)
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
      val jsonSchemaShapeContext = WebApiShapeParserContextAdapter(jsonSchemaContext)

      val fullRef = UriUtils.normalizePath(jsonSchemaContext.rootContextDocument)

      val tmpShape: UnresolvedShape =
        JsonSchemaParsingHelper.createTemporaryShape(
          _ => {},
          schemaEntry,
          jsonSchemaShapeContext,
          fullRef
        )

      val s = actualParsing(value, schemaEntry, jsonSchemaShapeContext, fullRef, tmpShape)
      savePromotedFragmentsFromNestedContext(jsonSchemaContext)
      s
    }
    shape
  }

  private def parseAst(text: String, valueAST: YNode, extLocation: Option[String]) = {
    val node = searchForAlreadyParsedNodeInFragments(valueAST)(shapeCtx).getOrElse {
      jsonParser(text, valueAST, extLocation).document().node
    }
    node
  }

  private def jsonParser(text: String, valueAST: YNode): JsonParser = {
    JsonParserFactory.fromCharsWithSource(
      text,
      valueAST.value.sourceName,
      Position(valueAST.range.lineFrom, valueAST.range.columnFrom)
    )(ctx.eh)
  }
  private def jsonParser(text: String, valueAST: YNode, extLocation: Option[String]): JsonParser = {
    val url = extLocation.flatMap(ctx.declarations.fragments.get).flatMap(_.location)
    url
      .map { JsonParserFactory.fromCharsWithSource(text, _)(ctx.eh) }
      .getOrElse(jsonParser(text, valueAST))
  }

  private def savePromotedFragmentsFromNestedContext(jsonSchemaContext: OasWebApiContext): Unit = {
    if (jsonSchemaContext.declarations.promotedFragments.nonEmpty) {
      ctx.declarations.promotedFragments ++= jsonSchemaContext.declarations.promotedFragments
    }
  }

  private def actualParsing(
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
