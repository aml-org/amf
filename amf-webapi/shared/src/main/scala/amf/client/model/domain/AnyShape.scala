package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
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

  override def linkTarget: Option[DomainElement] = _internal.linkTarget.map({ case s: InternalAnyShape => s }).asClient

  override def linkCopy(): AnyShape = _internal.linkCopy()
//  def build(shape: InternalAnyShape): Shape = platform.wrap[Shape](shape) ???

  def toJsonSchema: String = _internal.toJsonSchema
}
