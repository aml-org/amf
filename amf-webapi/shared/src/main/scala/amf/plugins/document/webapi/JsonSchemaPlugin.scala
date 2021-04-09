package amf.plugins.document.webapi

import amf.client.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Obj
import amf.core.model.document._
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{ParsedReference, ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.remote.{JsonSchema, Platform}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.annotations.JSONSchemaRoot
import amf.plugins.document.webapi.contexts.parser.oas.{JsonSchemaWebApiContext, OasWebApiContext}
import amf.plugins.document.webapi.parser.spec.OasWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaUnspecifiedVersion
}
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaParser
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model._

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaPlugin extends AMFDocumentPlugin with PlatformSecrets {
  override val vendors: Seq[String] = Seq(JsonSchema.name)

  override def modelEntities: Seq[Obj] = Nil

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map.empty

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq("application/schema+json", "application/payload+json")

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root, parentContext: ParserContext, options: ParsingOptions): BaseUnit = {
    val ctx = context(document.location, document.references, options, parentContext)
    new JsonSchemaParser().parse(document, ctx, options)
  }

  def context(loc: String,
              refs: Seq[ParsedReference],
              options: ParsingOptions,
              wrapped: ParserContext,
              ds: Option[OasWebApiDeclarations] = None): JsonSchemaWebApiContext = {
    // todo: we can set this default as this plugin is hardcoded to not parse
    // todo 2: we should debate the default version to use in the Plugin if we are to use it.
    new JsonSchemaWebApiContext(loc, refs, wrapped, ds, options, JSONSchemaUnspecifiedVersion)
  }

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: ErrorHandler): Option[YDocument] =
    unit match {
      case d: DeclaresModel =>
        // The root element of the JSON Schema must be identified with the annotation [[JSONSchemaRoot]]
        val root = d.declares.find(d => d.annotations.contains(classOf[JSONSchemaRoot]) && d.isInstanceOf[AnyShape])
        root match {
          case Some(r: AnyShape) =>
            Some(
              JsonSchemaEmitter(r, d.declares, options = renderOptions.shapeRenderOptions, errorHandler = errorHandler)
                .emitDocument())
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
