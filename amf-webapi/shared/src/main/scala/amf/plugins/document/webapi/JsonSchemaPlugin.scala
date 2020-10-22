package amf.plugins.document.webapi

import amf.client.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Obj
import amf.core.model.document._
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{EmptyFutureDeclarations, JsonParserFactory, ParsedReference, ParserContext, Reference, ReferenceHandler, SchemaReference, SimpleReferenceHandler, SyamlParsedDocument}
import amf.core.remote.{JsonSchema, Platform}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.unsafe.PlatformSecrets
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.annotations.JSONSchemaRoot
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.{JsonSchemaWebApiContext, OasWebApiContext}
import amf.plugins.document.webapi.contexts.parser.raml.Raml08WebApiContext
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{JsonSchemaEmitter, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.document.webapi.parser.spec.domain.OasParameter
import amf.plugins.document.webapi.parser.spec.jsonschema.{AstFinder, JsonSchemaFragmentParser, JsonSchemaParser}
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import amf.validations.ParserSideValidations.UnableToParseJsonSchema
import org.yaml.model._
import org.yaml.parser.{YParser, YamlParser}

import scala.concurrent.{ExecutionContext, Future}

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

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root,
                     parentContext: ParserContext,
                     platform: Platform,
                     options: ParsingOptions): Option[BaseUnit] = {
    new JsonSchemaParser().parse(document, parentContext, options)
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

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future.successful(this)

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = true
}

object JsonSchemaPlugin extends JsonSchemaPlugin
