package amf.client.plugins

import amf.core.emitter.RenderOptions
import amf.core.Root
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{ParserContext, ReferenceHandler}
import amf.core.registries.AMFDomainEntityResolver
import amf.core.remote.Platform
import amf.core.resolution.pipelines.ResolutionPipeline
import org.yaml.model.YDocument

object AMFDocumentPluginSettings {
  object PluginPriorities {
    val high    = 0
    val default = 5
    val low     = 10
  }
}

abstract class AMFDocumentPlugin extends AMFPlugin {

  /**
    * Does references in this type of documents be recursive?
    */
  val allowRecursiveReferences: Boolean

  // Parameter modifying which plugins are tried first
  val priority: Int = AMFDocumentPluginSettings.PluginPriorities.default

  val vendors: Seq[String]

  def modelEntities: Seq[Obj]

  def modelEntitiesResolver: Option[AMFDomainEntityResolver] = None

  def serializableAnnotations(): Map[String, AnnotationGraphLoader]

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  def resolve(unit: BaseUnit, pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  def documentSyntaxes: Seq[String]

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  def parse(document: Root, ctx: ParserContext, platform: Platform): Option[BaseUnit]

  /**
    * Unparses a model base unit and return a document AST
    */
  def unparse(unit: BaseUnit, options: RenderOptions): Option[YDocument]

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information from
    * the document structure
    */
  def canParse(document: Root): Boolean

  /**
    * Decides if this plugin can unparse the provided model document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will unparse the document base on information from
    * the instance type and properties
    */
  def canUnparse(unit: BaseUnit): Boolean

  def referenceHandler(): ReferenceHandler
}
