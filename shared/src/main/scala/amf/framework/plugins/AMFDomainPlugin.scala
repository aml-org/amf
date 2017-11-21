package amf.framework.plugins

import amf.compiler.AbstractReferenceCollector
import amf.core.Root
import amf.document.BaseUnit
import amf.spec.ParserContext

abstract class AMFDomainPlugin {

  val ID: String

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  def domainSyntaxes: Seq[String]

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  def parse(document: Root, ctx: ParserContext): Option[BaseUnit]

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  def accept(document: Root): Boolean

  def referenceCollector(): AbstractReferenceCollector
}
