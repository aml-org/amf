package amf.client.model.domain

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.model.StrField
import amf.core.unsafe.PlatformSecrets
import amf.plugins.domain.shapes.models.{AnyShape => InternalAnyShape}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

class AnyShape(override private[amf] val _internal: InternalAnyShape) extends Shape with PlatformSecrets {

  @JSExportTopLevel("model.domain.AnyShape")
  def this() = this(InternalAnyShape())

  @JSExport
  def documentation: CreativeWork = _internal.documentation
  @JSExport
  def xmlSerialization: XMLSerializer = _internal.xmlSerialization
  @JSExport
  def examples: ClientList[Example] = _internal.examples.asClient
  @JSExport
  def comment: StrField = _internal.comment

  @JSExport
  def withDocumentation(documentation: CreativeWork): this.type = {
    _internal.withDocumentation(documentation)
    this
  }

  @JSExport
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = {
    _internal.withXMLSerialization(xmlSerialization)
    this
  }

  @JSExport
  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }

  @JSExport
  def withComment(comment: String): this.type = {
    _internal.withComment(comment)
    this
  }

  @JSExport
  def withExample(mediaType: String): Example = _internal.withExample(Some(mediaType))

  @JSExport
  override def linkCopy(): AnyShape = _internal.linkCopy()

  /** Aux method to know when the shape is instance only of any shape
    * and it's because was parsed from
    * an empty (or only with example) payload, an not an explicit type def */
  @JSExport
  def isDefaultEmpty: Boolean = _internal.isDefaultEmpty

  /**
    * @param trackId parent id of the original payload type with simpleinheritance and the searched example.
    * @return A ClientOption of the original inlined example, or empty if there is not any example with
    *         that track information annotated.
    */
  @JSExport
  def trackedExample(trackId: String): ClientOption[Example] = _internal.trackedExample(trackId).asClient

  // Aux method to know if the shape has the annotation of [[InlineDefinition]]
  @JSExport
  def inlined(): Boolean = _internal.inlined
}
