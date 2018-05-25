package amf.plugins.document.webapi

import amf.core.Root
import amf.core.emitter.RenderOptions
import amf.core.metamodel.Obj
import amf.core.model.document._
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{EmptyFutureDeclarations, ParsedDocument, ParsedReference, ParserContext, Reference, ReferenceHandler, SchemaReference, SimpleReferenceHandler}
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.remote.{Oas3, Platform, Vendor}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaVersion, OasTypeParser}
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.document.webapi.parser.spec.{SpecSyntax, WebApiDeclarations}
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.concurrent.Future

class JsonSchemaWebApiContext(loc: String,
                              refs: Seq[ParsedReference],
                              private val wrapped: ParserContext,
                              private val ds: Option[WebApiDeclarations])
    extends OasWebApiContext(loc, refs, wrapped, ds) {
  override val factory: OasSpecVersionFactory = Oas3VersionFactory()(this)
  override val syntax: SpecSyntax             = Oas3Syntax
  override val vendor: Vendor                 = Oas3
}

class JsonSchemaPlugin extends AMFDocumentPlugin with PlatformSecrets {
  override val vendors: Seq[String] = Seq("JSON Schema")

  override def modelEntities: Seq[Obj] = Nil

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map.empty

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit, pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit =
    new OasResolutionPipeline().resolve(unit)

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq("application/schema+json")

  def parseFragment(inputFragment: Fragment, pointer: Option[String])(
      implicit ctx: OasWebApiContext): Option[AnyShape] = {
    val encoded: YNode = inputFragment match {
      case fragment: ExternalFragment =>
        fragment.encodes.parsed.getOrElse {
          YamlParser(fragment.encodes.raw.value())(ctx)
            .withIncludeTag("!include")
            .parse(keepTokens = true)
            .head match {
            case doc: YDocument => doc.node
            case _ =>
              ctx.violation(ParserSideValidations.ParsingErrorSpecification.id(),
                            inputFragment.id,
                            None,
                            "Cannot parse JSON Schema from unit with missing syntax information",
                            None)
              YNode(YMap(IndexedSeq()))
          }
        }
      case fragment: RecursiveUnit if fragment.raw.isDefined =>
        YamlParser(fragment.raw.get)(ctx)
          .withIncludeTag("!include")
          .parse(keepTokens = true)
          .head
          .asInstanceOf[YDocument]
          .node
      case _ =>
        ctx.violation(ParserSideValidations.ParsingErrorSpecification.id(),
                      inputFragment.id,
                      None,
                      "Cannot parse JSON Schema from unit with missing syntax information",
                      None)
        YNode(YMap(IndexedSeq()))
    }

    val doc = Root(
      ParsedDocument(
        None,
        YDocument(encoded)
      ),
      inputFragment.location + (if (pointer.isDefined) s"#${pointer.get}" else ""),
      "application/json",
      inputFragment.references.map(ref => ParsedReference(ref, Reference(ref.location, Nil), None)),
      SchemaReference,
      ExternalJsonRefsPlugin.ID,
      inputFragment.raw.getOrElse("")
    )
    parse(doc, ctx, platform).flatMap { parsed =>
      parsed match {
        case encoded: EncodesModel if encoded.encodes.isInstanceOf[AnyShape] =>
          Some(encoded.encodes.asInstanceOf[AnyShape])
        case _ => None
      }
    }
  }

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    val parts        = document.location.split("#")
    val shapeId      = if (document.location.contains("#")) document.location else document.location + "#/"
    val url          = parts.head
    val hashFragment = parts.tail.headOption

    val cleanNested =
      ParserContext(url, document.references, EmptyFutureDeclarations(), parserCount = parentContext.parserCount)
    cleanNested.globalSpace = parentContext.globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations =
      if (parentContext.isInstanceOf[Raml08WebApiContext]) Some(parentContext.asInstanceOf[WebApiContext].declarations)
      else None
    val jsonSchemaContext = new JsonSchemaWebApiContext(url, document.references, cleanNested, inheritedDeclarations)

    val documentRoot = document.parsed.document.node
    val rootAst = findRootNode(documentRoot, jsonSchemaContext, hashFragment).getOrElse {
      jsonSchemaContext.violation(shapeId,
                                  s"Cannot find fragment $url in JSON schema ${document.location}",
                                  documentRoot)
      documentRoot
    }

    jsonSchemaContext.localJSONSchemaContext = Some(documentRoot)
    val parsed =
      OasTypeParser(YMapEntry("schema", rootAst), (shape) => shape.withId(shapeId), version = JSONSchemaVersion)(
        jsonSchemaContext).parse() match {
        case Some(shape) =>
          shape
        case None =>
          jsonSchemaContext.violation(shapeId, s"Cannot parse JSON Schema at ${document.location}", rootAst)
          SchemaShape().withId(shapeId).withMediaType("application/json").withRaw(document.raw)
      }
    jsonSchemaContext.localJSONSchemaContext = None

    val unit: DataTypeFragment =
      DataTypeFragment().withId(document.location).withLocation(document.location).withEncodes(parsed)
    unit.withRaw(document.raw)
    Some(unit)
  }

  def findRootNode(ast: YNode, ctx: JsonSchemaWebApiContext, path: Option[String]) = {
    if (path.isDefined) {
      ctx.localJSONSchemaContext = Some(ast)
      val res = ctx.findLocalJSONPath(path.get)
      ctx.localJSONSchemaContext = None
      res.map(_._2)
    } else {
      Some(ast)
    }
  }

  /**
    * Unparses a model base unit and return a document AST
    */
  override def unparse(unit: BaseUnit, options: RenderOptions): Option[YDocument] =
    firstAnyShape(unit).map(as => JsonSchemaEmitter(as).emitDocument())

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

  override def referenceHandler(): ReferenceHandler = SimpleReferenceHandler

  override val ID: String = "JSON Schema" //version?

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def init(): Future[AMFPlugin] = Future.successful(this)

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = true
}

object JsonSchemaPlugin extends JsonSchemaPlugin
