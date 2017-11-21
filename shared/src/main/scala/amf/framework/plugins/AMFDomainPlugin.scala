package amf.framework.plugins

import amf.client.GenerationOptions
import amf.compiler.AbstractReferenceCollector
import amf.core.Root
import amf.document.BaseUnit
import amf.remote.Platform
import amf.spec.ParserContext
import org.yaml.model.YDocument

abstract class AMFDomainPlugin {

  val ID: String

  val vendors: Seq[String]

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  def domainSyntaxes: Seq[String]

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
