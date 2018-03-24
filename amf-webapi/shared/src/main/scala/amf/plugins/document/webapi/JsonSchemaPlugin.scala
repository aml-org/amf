package amf.plugins.document.webapi

import amf.client.render.RenderOptions
import amf.core.Root
import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{ParserContext, ReferenceHandler, SimpleReferenceHandler}
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.remote.Platform
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaEmitter
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument

import scala.concurrent.Future

object JsonSchemaPlugin extends AMFDocumentPlugin {
  override val vendors: Seq[String] = Seq("JSON Schema")

  override def modelEntities: Seq[Obj] = Nil

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map.empty

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit, pipelineId: String =  ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit = new OasResolutionPipeline().resolve(unit)

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq("application/schema+json")

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root, ctx: ParserContext, platform: Platform): Option[BaseUnit] = None

  /**
    * Unparses a model base unit and return a document AST
    */
  override def unparse(unit: BaseUnit, options: RenderOptions): Option[YDocument] = {
    firstAnyShape(unit).map(as => JsonSchemaEmitter(as).emitDocument())

  }

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information from
    * the document structure
    */
  override def canParse(document: Root): Boolean =
    false // we can parse a sub set of json schema here? oas type declared parser?

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
}
