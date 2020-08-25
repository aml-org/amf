package amf.plugins.document.webapi.parser.spec.declaration.external.raml

import amf.core.Root
import amf.core.annotations.ExternalFragmentRef
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.parser.{
  Annotations,
  InferredLinkReference,
  ParsedReference,
  Reference,
  ReferenceFragmentPartition,
  SyamlParsedDocument
}
import amf.plugins.document.webapi.annotations.{
  JSONSchemaId,
  ParsedJSONSchema,
  ExternalReferenceUrl,
  SchemaIsJsonSchema
}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.domain.NodeDataNodeParser
import amf.plugins.document.webapi.parser.spec.oas.Oas2DocumentParser
import amf.plugins.document.webapi.parser.spec.toJsonSchema
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape, UnresolvedShape}
import amf.validations.ParserSideValidations.{JsonSchemaFragmentNotFound, UnableToParseJsonSchema}
import org.mulesoft.lexer.Position
import org.yaml.model.YNode.MutRef
import org.yaml.model._
import org.yaml.parser.JsonParser
import amf.core.utils.AmfStrings
import amf.core.parser.YMapOps
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.document.webapi.parser.spec.declaration.utils.JsonSchemaParsingHelper

import scala.collection.mutable

case class RamlJsonSchemaExpression(key: YNode,
                                    override val value: YNode,
                                    override val adopt: Shape => Unit,
                                    parseExample: Boolean = false)(override implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {

  override def parseValue(origin: ValueAndOrigin): AnyShape = {
    val parsed: AnyShape = origin.oriUrl match {
      case Some(url) =>
        parseValueWithUrl(origin, url).add(ExternalReferenceUrl(url))
      case None =>
        val shape = parseJsonShape(origin.text, key, origin.valueAST, adopt, value, None)
        shape.annotations += ParsedJSONSchema(origin.text)
        shape
    }

    // parsing the potential example
    if (parseExample && value.tagType == YType.Map) {
      val map = value.as[YMap]

      map.key("displayName", (ShapeModel.DisplayName in parsed).allowingAnnotations)
      map.key("description", (ShapeModel.Description in parsed).allowingAnnotations)
      map.key(
        "default",
        entry => {
          val dataNodeResult = NodeDataNodeParser(entry.value, parsed.id, quiet = false).parse()
          parsed.setDefaultStrValue(entry)
          dataNodeResult.dataNode.foreach { dataNode =>
            parsed.set(ShapeModel.Default, dataNode, Annotations(entry))
          }
        }
      )
      parseExamples(parsed, value.as[YMap])
    }

    parsed.annotations += SchemaIsJsonSchema()
    parsed
  }

  private def parseValueWithUrl(origin: ValueAndOrigin, url: String) = {
    val (basePath, localPath) = ReferenceFragmentPartition(url)
    val normalizedLocalPath   = localPath.map(_.stripPrefix("/definitions/"))
    normalizedLocalPath
      .flatMap(ctx.declarations.findInExternalsLibs(basePath, _))
      .orElse(ctx.declarations.findInExternals(basePath)) match {
      case Some(s) =>
        val shape = s.copyShape().withName(key.as[String])
        ctx.declarations.fragments
          .get(basePath)
          .foreach(e => shape.withReference(e.encoded.id + localPath.getOrElse("")))
        if (shape.examples.nonEmpty) { // top level inlined shape, we don't want to reuse the ID, this must be an included JSON schema => EDGE CASE!
          shape.id = null // <-- suspicious
          adopt(shape)
          // We remove the examples declared in the previous endpoint for this inlined shape , see previous comment about the edge case
          shape.fields.remove(AnyShapeModel.Examples.value.iri())
        }
        shape
      case _ if normalizedLocalPath.isDefined => // oas lib
        parseOasLib(origin, basePath, localPath, normalizedLocalPath)
      case _ =>
        parseFragment(origin, basePath)
    }
  }

  private def parseOasLib(origin: ValueAndOrigin,
                          basePath: String,
                          localPath: Option[String],
                          normalizedLocalPath: Option[String]) = {
    RamlExternalOasLibParser(ctx, origin.text, origin.valueAST, basePath).parse()
    val shape = ctx.declarations.findInExternalsLibs(basePath, normalizedLocalPath.get) match {
      case Some(s) =>
        s.copyShape().withName(key.as[String])
      case _ =>
        val empty = AnyShape()
        adopt(empty)
        ctx.eh.violation(JsonSchemaFragmentNotFound,
                         empty.id,
                         s"could not find json schema fragment ${localPath.get} in file $basePath",
                         origin.valueAST)
        empty

    }
    ctx.declarations.fragments
      .get(basePath)
      .foreach(e => shape.withReference(e.encoded.id + localPath.get))

    shape.annotations += ExternalFragmentRef(localPath.get)
    shape
  }

  private def parseFragment(origin: ValueAndOrigin, basePath: String) = {
    val shape = parseJsonShape(origin.text, key, origin.valueAST, adopt, value, origin.oriUrl)
    ctx.declarations.fragments
      .get(basePath)
      .foreach(e => shape.withReference(e.encoded.id))
    ctx.declarations.registerExternalRef(basePath, shape)
    shape.annotations += ParsedJSONSchema(origin.text.trim)
    shape
  }

  case class RamlExternalOasLibParser(ctx: RamlWebApiContext, text: String, valueAST: YNode, path: String) {
    def parse(): Unit = {
      // todo: should we add string begin position to each node position? in order to have the positions relatives to root api intead of absolut to text
      val url               = path.normalizeUrl + (if (!path.endsWith("/")) "/" else "") // alwarys add / to avoid ask if there is any one before add #
      val schemaEntry       = JsonParser.withSource(text, valueAST.sourceName)(ctx.eh).document()
      val jsonSchemaContext = toSchemaContext(ctx, valueAST)
      jsonSchemaContext.localJSONSchemaContext = Some(schemaEntry.node)
      jsonSchemaContext.setJsonSchemaAST(schemaEntry.node)

      Oas2DocumentParser(
        Root(SyamlParsedDocument(schemaEntry), url, "application/json", Nil, InferredLinkReference, text))(
        jsonSchemaContext)
        .parseTypeDeclarations(schemaEntry.node.as[YMap], url + "#/definitions/")(jsonSchemaContext)
      val resolvedShapes = jsonSchemaContext.declarations.shapes.values.toSeq
      val shapesMap      = mutable.Map[String, AnyShape]()
      resolvedShapes.map(s => (s, s.annotations.find(classOf[JSONSchemaId]))).foreach {
        case (s: AnyShape, Some(a)) if a.id.equals(s.name.value()) =>
          shapesMap += s.name.value -> s
        case (s: AnyShape, Some(a)) =>
          shapesMap += s.name.value() -> s
          shapesMap += a.id           -> s
        case (s: AnyShape, None) => shapesMap += s.name.value -> s
      }

      ctx.declarations.registerExternalLib(path, shapesMap.toMap)
    }
  }

  private def parseJsonShape(text: String,
                             key: YNode,
                             valueAST: YNode,
                             adopt: Shape => Unit,
                             value: YNode,
                             extLocation: Option[String]): AnyShape = {

    val url               = extLocation.flatMap(ctx.declarations.fragments.get).flatMap(_.location)
    val parser            = getParser(text, valueAST, url)
    val schemaEntry       = YMapEntry(key, parser.document().node)
    val jsonSchemaContext = getContext(valueAST, schemaEntry)
    val fullRef           = jsonSchemaContext.resolvedPath(jsonSchemaContext.rootContextDocument, "")

    val tmpShape: UnresolvedShape =
      JsonSchemaParsingHelper.createTemporaryShape(shape => adopt(shape), schemaEntry, jsonSchemaContext, fullRef)

    val s = actualParsing(adopt, value, schemaEntry, jsonSchemaContext, fullRef, tmpShape)

    cleanGlobalSpace()
    savePromotedFragmentsFromNestedContext(jsonSchemaContext)
    s
  }

  private def getContext(valueAST: YNode, schemaEntry: YMapEntry) = {
    // we set the local schema entry to be able to resolve local $refs
    ctx.setJsonSchemaAST(schemaEntry.value)
    toSchemaContext(ctx, valueAST)
  }

  /**
    * Clean from globalSpace the local references
    */
  private def cleanGlobalSpace(): Unit = {
    ctx.globalSpace.foreach { e =>
      val refPath = e._1.split("#").headOption.getOrElse("")
      if (refPath == ctx.localJSONSchemaContext.get.sourceName) ctx.globalSpace.remove(e._1)
    }
  }

  private def savePromotedFragmentsFromNestedContext(jsonSchemaContext: OasWebApiContext): Unit = {
    if (jsonSchemaContext.declarations.promotedFragments.nonEmpty) {
      ctx.declarations.promotedFragments ++= jsonSchemaContext.declarations.promotedFragments
    }
    ctx.localJSONSchemaContext = None // we reset the JSON schema context after parsing
  }

  private def getParser(text: String, valueAST: YNode, url: Option[String]) = {
    if (url.isDefined)
      JsonParser.withSource(text, url.get)(ctx.eh)
    else
      JsonParser.withSource(text,
                            valueAST.value.sourceName,
                            Position(valueAST.range.lineFrom, valueAST.range.columnFrom))(ctx.eh)
  }
  private def actualParsing(adopt: Shape => Unit,
                            value: YNode,
                            schemaEntry: YMapEntry,
                            jsonSchemaContext: OasWebApiContext,
                            fullRef: String,
                            tmpShape: UnresolvedShape) = {
    OasTypeParser(schemaEntry, shape => adopt(shape), ctx.computeJsonSchemaVersion(schemaEntry.value))(
      jsonSchemaContext)
      .parse() match {
      case Some(sh) =>
        ctx.futureDeclarations.resolveRef(fullRef, sh)
        ctx.registerJsonSchema(fullRef, sh)
        tmpShape.resolve(sh) // useless?
        if (sh.isLink) sh.effectiveLinkTarget().asInstanceOf[AnyShape]
        else sh
      case None =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.eh.violation(UnableToParseJsonSchema, shape.id, "Cannot parse JSON Schema", value)
        shape
    }
  }
  protected def toSchemaContext(ctx: WebApiContext, ast: YNode): OasWebApiContext = {
    ast match {
      case inlined: MutRef =>
        if (inlined.origTag.tagType == YType.Include) {
          // JSON schema file we need to update the context
          val rawPath            = inlined.origValue.asInstanceOf[YScalar].text
          val normalizedFilePath = normalizePath(rawPath)
          ctx.refs.find(r => r.unit.location().exists(_.endsWith(normalizedFilePath))) match {
            case Some(ref) =>
              toJsonSchema(
                ref.unit.location().get,
                ref.unit.references.map(r => ParsedReference(r, Reference(ref.unit.location().get, Nil), None)),
                ctx)
            case _
                if Option(ast.value.sourceName).isDefined => // external fragment from external fragment case. The target value ast has the real source name of the faile. (There is no external fragment because was inlined)
              toJsonSchema(ast.value.sourceName, ctx.refs, ctx)
            case _ => toJsonSchema(ctx)
          }
        } else {
          // Inlined we don't need to update the context for ths JSON schema file
          toJsonSchema(ctx)
        }
      case _ =>
        toJsonSchema(ctx)
    }
  }

  private def normalizePath(rawPath: String): String = {
    //    TODO: we need to resolve paths but this conflicts with absolute references to exchange_modules
    //    val file = rawPath.split("#").head
    //    val root               = ctx.rootContextDocument
    //    val normalizedFilePath = ctx.resolvedPath(root, file)
    val hashTagIdx = rawPath.indexOf("#")
    val parentIdx  = rawPath.lastIndexOf("../") + 3
    val currentIdx = rawPath.lastIndexOf("./") + 2
    val start      = parentIdx.max(currentIdx).max(0)
    val finish     = if (hashTagIdx == -1) rawPath.length else hashTagIdx
    rawPath.substring(start, finish)
  }

  override val externalType: String = "JSON"
}
