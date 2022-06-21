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
    val parsed = parseJsonFromValueAndOrigin(origin)
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
    val parsed = parseJsonFromValueAndOrigin(origin)
    parsed.annotations += SchemaIsJsonSchema()
    parsed.withName("schema")
    parsed
  }

  private def parseJsonFromValueAndOrigin(origin: ValueAndOrigin) = {
    origin.originalUrlText match {
      case Some(url) =>
        parseIncludedSchema(origin, url).add(ExternalReferenceUrl(url))
      case None =>
        InlineJsonSchemaParser.parse(key, value, origin)
    }
  }

  private def parseIncludedSchema(origin: ValueAndOrigin, url: String) = {
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
    val shape = parseJsonShape(origin.text, key, origin.valueAST, value, origin.originalUrlText)
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

  override val externalType: String = "JSON"
}
