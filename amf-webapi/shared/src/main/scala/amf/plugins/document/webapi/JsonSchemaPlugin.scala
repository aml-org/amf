package amf.plugins.document.webapi

import amf.client.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.metamodel.Obj
import amf.core.model.document._
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{
  EmptyFutureDeclarations,
  ErrorHandler,
  ParsedReference,
  ParserContext,
  Reference,
  ReferenceHandler,
  SchemaReference,
  SimpleReferenceHandler,
  SyamlParsedDocument
}
import amf.core.remote.{JsonSchema, Platform, Vendor}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.annotations.JSONSchemaRoot
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaEmitter
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.document.webapi.parser.spec.domain.OasParameter
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.document.webapi.parser.spec.{OasWebApiDeclarations, SpecSyntax, _}
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ParserSideValidations.UnableToParseJsonSchema
import org.yaml.model._
import org.yaml.parser.JsonParser

import scala.concurrent.Future

class JsonSchemaWebApiContext(loc: String,
                              refs: Seq[ParsedReference],
                              private val wrapped: ParserContext,
                              private val ds: Option[OasWebApiDeclarations],
                              parserCount: Option[Int] = None,
                              override val eh: Option[ErrorHandler] = None)
    extends OasWebApiContext(loc, refs, wrapped, ds, parserCount, eh) {
  override val factory: OasSpecVersionFactory = Oas3VersionFactory()(this)
  override val syntax: SpecSyntax             = Oas3Syntax
  override val vendor: Vendor                 = JsonSchema
  override val linkTypes: Boolean = wrapped match {
    case _: RamlWebApiContext => false
    case _: OasWebApiContext  => true // definitions tag
    case _                    => false
  } // oas definitions
}

class JsonSchemaPlugin extends AMFDocumentPlugin with PlatformSecrets {
  override val vendors: Seq[String] = Seq(JsonSchema.name)

  override def modelEntities: Seq[Obj] = Nil

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map.empty

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit,
                       errorHandler: ErrorHandler,
                       pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit =
    new OasResolutionPipeline(errorHandler).resolve(unit)

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq("application/schema+json", "application/payload+json")

  def parseFragment(inputFragment: Fragment, pointer: Option[String])(
      implicit ctx: OasWebApiContext): Option[AnyShape] = {

    val encoded: YNode = getYNode(inputFragment, ctx)
    val doc: Root      = getRoot(inputFragment, pointer, encoded)

    parse(doc, ctx, platform, new ParsingOptions()).flatMap { parsed =>
      parsed match {
        case encoded: EncodesModel if encoded.encodes.isInstanceOf[AnyShape] =>
          Some(encoded.encodes.asInstanceOf[AnyShape])
        case _ => None
      }
    }
  }

  def parseParameterFragment(inputFragment: Fragment,
                             pointer: Option[String],
                             parentId: String,
                             jsonContext: OasWebApiContext)(implicit ctx: OasWebApiContext): Option[OasParameter] = {

    val encoded: YNode = getYNode(inputFragment, ctx)
    val doc: Root      = getRoot(inputFragment, pointer, encoded)

    parseOasParameter(doc, ctx, parentId, jsonContext)

  }

  private def getYNode(inputFragment: Fragment, ctx: WebApiContext): YNode = {
    inputFragment match {
      case fragment: ExternalFragment =>
        fragment.encodes.parsed.getOrElse {
          JsonParser
            .withSource(fragment.encodes.raw.value(), fragment.location().getOrElse(""))(ctx)
            .parse(keepTokens = true)
            .head match {
            case doc: YDocument => doc.node
            case _ =>
              ctx.violation(UnableToParseJsonSchema,
                            inputFragment,
                            None,
                            "Cannot parse JSON Schema from unit with missing syntax information")
              YNode(YMap(IndexedSeq(), ""))
          }
        }
      case fragment: RecursiveUnit if fragment.raw.isDefined =>
        JsonParser
          .withSource(fragment.raw.get, fragment.location().getOrElse(""))(ctx)
          .parse(keepTokens = true)
          .head
          .asInstanceOf[YDocument]
          .node
      case _ =>
        ctx.violation(UnableToParseJsonSchema,
                      inputFragment,
                      None,
                      "Cannot parse JSON Schema from unit with missing syntax information")
        YNode(YMap(IndexedSeq(), ""))
    }
  }

  private def getRoot(inputFragment: Fragment, pointer: Option[String], encoded: YNode): Root = {
    Root(
      SyamlParsedDocument(YDocument(encoded)),
      inputFragment.location().getOrElse(inputFragment.id) + (if (pointer.isDefined) s"#${pointer.get}" else ""),
      "application/json",
      inputFragment.references.map(ref => ParsedReference(ref, Reference(ref.location().getOrElse(""), Nil), None)),
      SchemaReference,
      inputFragment.raw.getOrElse("")
    )
  }

  private def getJsonSchemaContext(document: Root,
                                   parentContext: ParserContext,
                                   url: String): JsonSchemaWebApiContext = {
    val cleanNested =
      ParserContext(url, document.references, EmptyFutureDeclarations(), parserCount = parentContext.parserCount)
    cleanNested.globalSpace = parentContext.globalSpace
    cleanNested.reportDisambiguation = parentContext.reportDisambiguation

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations =
      if (parentContext.isInstanceOf[Raml08WebApiContext])
        Some(parentContext.asInstanceOf[WebApiContext].declarations)
      else None

    new JsonSchemaWebApiContext(url,
                                document.references,
                                cleanNested,
                                inheritedDeclarations.map(d => toOasDeclarations(d)))
  }

  private def getRootAst(document: Root,
                         parsedDoc: SyamlParsedDocument,
                         shapeId: String,
                         hashFragment: Option[String],
                         url: String,
                         jsonSchemaContext: JsonSchemaWebApiContext): YNode = {
    val documentRoot = parsedDoc.document.node
    val rootAst = findRootNode(documentRoot, jsonSchemaContext, hashFragment).getOrElse {
      jsonSchemaContext.violation(UnableToParseJsonSchema,
                                  shapeId,
                                  s"Cannot find fragment $url in JSON schema ${document.location}",
                                  documentRoot)
      documentRoot
    }

    jsonSchemaContext.setJsonSchemaAST(documentRoot)
    rootAst
  }

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root,
                     parentContext: ParserContext,
                     platform: Platform,
                     options: ParsingOptions): Option[BaseUnit] = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String      = if (document.location.contains("#")) document.location else document.location + "#/"
        val parts: Array[String] = document.location.split("#")
        val url: String          = parts.head
        val hashFragment: Option[String] =
          parts.tail.headOption.map(t => if (t.startsWith("/definitions")) t.stripPrefix("/") else t)

        val jsonSchemaContext = getJsonSchemaContext(document, parentContext, url)
        val rootAst           = getRootAst(document, parsedDoc, shapeId, hashFragment, url, jsonSchemaContext)

        val parsed =
          OasTypeParser(YMapEntry("schema", rootAst),
                        shape => shape.withId(shapeId),
                        version = jsonSchemaContext.computeJsonSchemaVersion(rootAst))(jsonSchemaContext)
            .parse() match {
            case Some(shape) =>
              shape
            case None =>
              jsonSchemaContext.violation(UnableToParseJsonSchema,
                                          shapeId,
                                          s"Cannot parse JSON Schema at ${document.location}",
                                          rootAst)
              SchemaShape().withId(shapeId).withMediaType("application/json").withRaw(document.raw)
          }
        jsonSchemaContext.localJSONSchemaContext = None

        val unit: DataTypeFragment =
          DataTypeFragment().withId(document.location).withLocation(document.location).withEncodes(parsed)
        unit.withRaw(document.raw)
        Some(unit)

      case _ => None
    }
  }

  def parseOasParameter(document: Root,
                        parentContext: ParserContext,
                        parentId: String,
                        ctx: OasWebApiContext): Option[OasParameter] = {

    document.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String      = if (document.location.contains("#")) document.location else document.location + "#/"
        val parts: Array[String] = document.location.split("#")
        val url: String          = parts.head
        val hashFragment: Option[String] =
          parts.tail.headOption.map(t => if (t.startsWith("/")) t.stripPrefix("/") else t)

        val jsonSchemaContext = getJsonSchemaContext(document, parentContext, url)
        val rootAst           = getRootAst(document, parsedDoc, shapeId, hashFragment, url, jsonSchemaContext)
        Some(ctx.factory.parameterParser(Right(rootAst), parentId, None, new IdCounter()).parse)

      case _ => None
    }
  }

  def findRootNode(ast: YNode, ctx: JsonSchemaWebApiContext, path: Option[String]): Option[YNode] = {
    if (path.isDefined) {
      ctx.setJsonSchemaAST(ast)
      val res = ctx.findLocalJSONPath(path.get)
      ctx.localJSONSchemaContext = None
      res.map(_._2)
    } else {
      Some(ast)
    }
  }

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderOptions: RenderOptions,
      shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Option[YDocument] =
    unit match {
      case d: DeclaresModel =>
        // The root element of the JSON Schema must be identified with the annotation [[JSONSchemaRoot]]
        val root = d.declares.find(d => d.annotations.contains(classOf[JSONSchemaRoot]) && d.isInstanceOf[AnyShape])
        root match {
          case Some(r: AnyShape) =>
            Some(JsonSchemaEmitter(r, d.declares, options = shapeRenderOptions).emitDocument())
          case _ => None
        }
      case _ => None
    }

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information from
    * the document structure
    */
  override def canParse(document: Root): Boolean =
    false // we invoke this explicitly, we don't want the registry to load it

  /**
    * Decides if this plugin can unparse the provided model document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will unparse the document base on information from
    * the instance type and properties
    */
  override def canUnparse(unit: BaseUnit): Boolean = firstAnyShape(unit).isDefined

  private def firstAnyShape(unit: BaseUnit): Option[AnyShape] = unit match {
    case d: DeclaresModel => d.declares.collectFirst({ case a: AnyShape => a })
    case _                => None
  }

  override def referenceHandler(eh: ErrorHandler): ReferenceHandler = SimpleReferenceHandler

  override val ID: String = "JSON Schema" // version?

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def init(): Future[AMFPlugin] = Future.successful(this)

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = true
}

object JsonSchemaPlugin extends JsonSchemaPlugin
