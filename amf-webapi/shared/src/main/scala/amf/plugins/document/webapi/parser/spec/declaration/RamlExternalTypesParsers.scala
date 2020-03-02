package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.Root
import amf.core.annotations.{LexicalInformation, ExternalFragmentRef}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.parser.{ParsedReference, InferredLinkReference, Reference, ReferenceFragmentPartition, Annotations, _}
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.annotations.{ParsedJSONSchema, JSONSchemaId, SchemaIsJsonSchema}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.domain.NodeDataNodeParser
import amf.plugins.document.webapi.parser.spec.oas.Oas2DocumentParser
import amf.plugins.document.webapi.parser.spec.raml.RamlSpecParser
import amf.plugins.document.webapi.parser.spec.toJsonSchema
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, SchemaShapeModel}
import amf.plugins.domain.shapes.models.{SchemaShape, AnyShape, UnresolvedShape}
import amf.validations.ParserSideValidations._
import org.yaml.model.YNode.MutRef
import org.yaml.model._
import org.yaml.parser.JsonParser
import org.mulesoft.lexer.Position

import scala.collection.mutable
case class RamlJsonSchemaExpression(key: YNode,
                                    override val value: YNode,
                                    override val adopt: Shape => Unit,
                                    parseExample: Boolean = false)(override implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {

  override def parseValue(origin: ValueAndOrigin): AnyShape = {
    val parsed: AnyShape = origin.oriUrl match {
      case Some(url) =>
        val (path, extFragment) = ReferenceFragmentPartition(url)
        val fragment            = extFragment.map(_.stripPrefix("/definitions/").stripPrefix("definitions/"))
        fragment
          .flatMap(ctx.declarations.findInExternalsLibs(path, _))
          .orElse(ctx.declarations.findInExternals(path)) match {
          case Some(s) =>
            val shape = s.copyShape().withName(key.as[String])
            ctx.declarations.fragments
              .get(path)
              .foreach(e => shape.withReference(e.encoded.id + extFragment.getOrElse("")))
            if (shape.examples.nonEmpty) { // top level inlined shape, we don't want to reuse the ID, this must be an included JSON schema => EDGE CASE!
              shape.id = null
              adopt(shape)
              // We remove the examples declared in the previous endpoint for this inlined shape , see previous comment about the edge case
              shape.fields.remove(AnyShapeModel.Examples.value.iri())
            }
            shape
          case _ if fragment.isDefined => // oas lib
            RamlExternalOasLibParser(ctx, origin.text, origin.valueAST, path).parse()
            val shape = ctx.declarations.findInExternalsLibs(path, fragment.get) match {
              case Some(s) =>
                s.copyShape().withName(key.as[String])
              case _ =>
                val empty = AnyShape()
                adopt(empty)
                ctx.eh.violation(JsonSchemaFragmentNotFound,
                                 empty.id,
                                 s"could not find json schema fragment ${extFragment.get} in file $path",
                                 origin.valueAST)
                empty

            }
            ctx.declarations.fragments
              .get(path)
              .foreach(e => shape.withReference(e.encoded.id + extFragment.get))

            shape.annotations += ExternalFragmentRef(extFragment.get)
            shape
          case _ =>
            val shape = parseJsonShape(origin.text, key, origin.valueAST, adopt, value, origin.oriUrl)
            ctx.declarations.fragments
              .get(path)
              .foreach(e => shape.withReference(e.encoded.id))
            ctx.declarations.registerExternalRef(path, shape)
            shape.annotations += ParsedJSONSchema(origin.text.trim)
            shape
        }
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
        .parseTypeDeclarations(schemaEntry.node.as[YMap], url + "#/definitions/")
      val libraryShapes = jsonSchemaContext.declarations.shapes
      val resolvedShapes = new ReferenceResolutionStage(false)(ctx.eh)
        .resolveDomainElementSet[Shape](libraryShapes.values.toSeq)

      val shapesMap = mutable.Map[String, AnyShape]()
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
    val url = extLocation.flatMap(ctx.declarations.fragments.get).flatMap(_.location)

    val parser =
      if (url.isDefined)
        JsonParser.withSource(text, url.get)(ctx.eh)
      else
        JsonParser.withSource(text,
                              valueAST.value.sourceName,
                              Position(valueAST.range.lineFrom, valueAST.range.columnFrom))(ctx.eh)

    val schemaEntry = YMapEntry(key, parser.document().node)

    // we set the local schema entry to be able to resolve local $refs
    ctx.setJsonSchemaAST(schemaEntry.value)
    val jsonSchemaContext = toSchemaContext(ctx, valueAST)
    val fullRef           = jsonSchemaContext.resolvedPath(jsonSchemaContext.rootContextDocument, "")
    val tmpShape =
      UnresolvedShape(fullRef, schemaEntry).withName(fullRef).withId(fullRef).withSupportsRecursion(true)
    tmpShape.unresolved(fullRef, schemaEntry, "warning")(jsonSchemaContext)
    tmpShape.withContext(jsonSchemaContext)
    adopt(tmpShape)
    ctx.registerJsonSchema(fullRef, tmpShape)

    val s =
      OasTypeParser(schemaEntry, shape => adopt(shape), ctx.computeJsonSchemaVersion(schemaEntry.value))(
        jsonSchemaContext)
        .parse() match {
        case Some(sh) =>
          ctx.futureDeclarations.resolveRef(fullRef, sh)
          tmpShape.resolve(sh) // useless?
          ctx.registerJsonSchema(fullRef, sh)
          if (sh.isLink) sh.effectiveLinkTarget().asInstanceOf[AnyShape]
          else sh
        case None =>
          val shape = SchemaShape()
          adopt(shape)
          ctx.eh.violation(UnableToParseJsonSchema, shape.id, "Cannot parse JSON Schema", value)
          shape
      }
    // we clean from globalSpace the local references
    ctx.globalSpace.foreach { e =>
      val refPath = e._1.split("#").headOption.getOrElse("")
      if (refPath == ctx.localJSONSchemaContext.get.sourceName) ctx.globalSpace.remove(e._1)
    }
    ctx.localJSONSchemaContext = None // we reset the JSON schema context after parsing
    s
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

case class RamlXmlSchemaExpression(key: YNode,
                                   override val value: YNode,
                                   override val adopt: Shape => Unit,
                                   parseExample: Boolean = false)(override implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {
  override def parseValue(origin: ValueAndOrigin): SchemaShape = {
    val (maybeReferenceId, maybeLocation, maybeFragmentLabel): (Option[String], Option[String], Option[String]) =
      origin.oriUrl
        .map(ReferenceFragmentPartition.apply) match {
        case Some((path, uri)) =>
          val maybeRef = ctx.declarations.fragments
            .get(path)
          (maybeRef
             .map(_.encoded.id + uri.map(u => if (u.startsWith("/")) u else "/" + u).getOrElse("")),
           maybeRef.flatMap(_.location),
           uri)
        case None => (None, None, None)
      }

    val parsed = value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        val parsedSchema = nestedTypeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            val shape =
              SchemaShape().withRaw(typeEntry.value.as[YScalar].text).withMediaType("application/xml")
            shape.withName(key.as[String])
            adopt(shape)
            shape
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.eh.violation(InvalidXmlSchemaType,
                             shape.id,
                             "Cannot parse XML Schema expression out of a non string value",
                             value)
            shape
        }
        map.key("displayName", (ShapeModel.DisplayName in parsedSchema).allowingAnnotations)
        map.key("description", (ShapeModel.Description in parsedSchema).allowingAnnotations)
        map.key(
          "default",
          entry => {
            val dataNodeResult = NodeDataNodeParser(entry.value, parsedSchema.id, quiet = false).parse()
            parsedSchema.setDefaultStrValue(entry)
            dataNodeResult.dataNode.foreach { dataNode =>
              parsedSchema.set(ShapeModel.Default, dataNode, Annotations(entry))
            }
          }
        )
        parseExamples(parsedSchema, value.as[YMap])

        parsedSchema
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.eh.violation(InvalidXmlSchemaType,
                         shape.id,
                         "Cannot parse XML Schema expression out of a non string value",
                         value)
        shape
      case _ =>
        val raw   = value.as[YScalar].text
        val shape = SchemaShape().withRaw(raw).withMediaType("application/xml")
        shape.withName(key.as[String])
        adopt(shape)
        shape
    }
    maybeReferenceId match {
      case Some(r) => parsed.withReference(r)
      case _       => parsed.annotations += LexicalInformation(Range(value.range))
    }
    parsed.set(SchemaShapeModel.Location, maybeLocation.getOrElse(ctx.loc))
    maybeFragmentLabel.foreach { parsed.annotations += ExternalFragmentRef(_) }
    parsed
  }

  override val externalType: String = "XML"
}

trait RamlExternalTypesParser extends RamlSpecParser with ExampleParser with RamlTypeSyntax {

  val value: YNode
  val adopt: Shape => Unit
  val externalType: String
  def parseValue(origin: ValueAndOrigin): AnyShape

  def parse(): AnyShape = {
    val origin = buildTextAndOrigin()
    origin.errorShape match {
      case Some(shape) => shape
      case _           => parseValue(origin)
    }
  }

  protected def getOrigin(node: YNode): Option[String] = node match {
    case ref: MutRef => Some(ref.origValue.toString)
    case _           => None
  }

  protected case class ValueAndOrigin(text: String,
                                      valueAST: YNode,
                                      oriUrl: Option[String],
                                      errorShape: Option[AnyShape] = None)

  protected def buildTextAndOrigin(): ValueAndOrigin = {
    value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        nestedTypeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            ValueAndOrigin(typeEntry.value.as[YScalar].text, typeEntry.value, getOrigin(typeEntry.value))
          case _ =>
            failSchemaExpressionParser
        }
      case YType.Seq =>
        failSchemaExpressionParser
      case _ => ValueAndOrigin(value.as[YScalar].text, value, getOrigin(value))
    }
  }

  private def failSchemaExpressionParser = {
    val shape = SchemaShape()
    adopt(shape)
    ctx.eh.violation(InvalidExternalTypeType,
                     shape.id,
                     s"Cannot parse $externalType Schema expression out of a non string value",
                     value)
    ValueAndOrigin("", value, None, Some(shape))
  }
}
