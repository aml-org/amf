package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.document.PayloadFragment
import amf.client.validate.ValidationReport
import amf.plugins.domain.shapes.models.{AnyShape => InternalAnyShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class AnyShape(override private[amf] val _internal: InternalAnyShape) extends Shape {

  @JSExportTopLevel("model.domain.AnyShape")
  def this() = this(InternalAnyShape())

  def documentation: CreativeWork     = _internal.documentation
  def xmlSerialization: XMLSerializer = _internal.xmlSerialization
  def examples: ClientList[Example]   = _internal.examples.asClient

  def withDocumentation(documentation: CreativeWork): this.type = {
    _internal.withDocumentation(documentation)
    this
  }

  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = {
    _internal.withXMLSerialization(xmlSerialization)
    this
  }

  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }

  def withExample(mediaType: String): Example = _internal.withExample(Some(mediaType))

  override def linkCopy(): AnyShape = _internal.linkCopy()
//  def build(shape: InternalAnyShape): Shape = platform.wrap[Shape](shape) ???

  def toJsonSchema: String = _internal.toJsonSchema

  def validate(payload: String): ClientFuture[ValidationReport] =
    _internal.validate(payload).asClient

  def validate(fragment: PayloadFragment): ClientFuture[ValidationReport] =
    _internal.validate(fragment._internal).asClient

  /** Aux method to know when the shape is instance only of any shape
    * and it's because was parsed from
    * an empty (or only with example) payload, an not an explicit type def */
  def isDefaultEmpty: Boolean = _internal.isDefaultEmpty
}
