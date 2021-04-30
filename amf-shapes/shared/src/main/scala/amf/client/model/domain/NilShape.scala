package amf.client.model.domain

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.plugins.domain.shapes.models.{NilShape => InternalNilShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class NilShape(override private[amf] val _internal: InternalNilShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.NilShape")
  def this() = this(InternalNilShape())

  override def linkCopy(): NilShape = _internal.linkCopy()
}
