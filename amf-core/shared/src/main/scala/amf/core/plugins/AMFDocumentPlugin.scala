package amf.core.plugins

import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{AbstractReferenceCollector, ParserContext}
import amf.core.remote.Platform
import org.yaml.model.YDocument

abstract class AMFDocumentPlugin extends AMFPlugin {

  val vendors: Seq[String]

  def modelEntities: Seq[Obj]

  def serializableAnnotations(): Map[String, AnnotationGraphLoader]

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  def resolve(unit: BaseUnit): BaseUnit

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
  def unparse(unit: BaseUnit, options: GenerationOptions): Option[YDocument]

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

  def referenceCollector(): AbstractReferenceCollector

}
