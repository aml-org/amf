package amf.shapes.client.platform.model.domain.operations

import amf.shapes.client.scala.model.domain.operations.{ShapePayload => InternalShapePayload}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ShapePayload(override private[amf] val _internal: InternalShapePayload) extends AbstractPayload(_internal) {

  override def linkCopy(): ShapePayload = _internal.linkCopy()

  @JSExportTopLevel("ShapePayload")
  def this() = this(InternalShapePayload())
}
