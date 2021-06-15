package amf.shapes.client.platform.model.domain

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.domain.models.{ NilShape => InternalNilShape }
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class NilShape(override private[amf] val _internal: InternalNilShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.NilShape")
  def this() = this(InternalNilShape())

  override def linkCopy(): NilShape = _internal.linkCopy()
}
