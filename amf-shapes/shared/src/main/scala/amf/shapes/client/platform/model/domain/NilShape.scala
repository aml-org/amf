package amf.shapes.client.platform.model.domain

import amf.shapes.client.scala.model.domain

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class NilShape(override private[amf] val _internal: domain.NilShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.NilShape")
  def this() = this(InternalNilShape())

  override def linkCopy(): NilShape = _internal.linkCopy()
}
