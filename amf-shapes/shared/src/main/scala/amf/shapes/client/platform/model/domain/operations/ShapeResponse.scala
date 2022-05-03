package amf.shapes.client.platform.model.domain.operations

import amf.shapes.client.scala.model.domain.operations.{ShapeResponse => InternalShapeResponse}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ShapeResponse(override private[amf] val _internal: InternalShapeResponse) extends AbstractResponse {

  override type PayloadType = ShapePayload

  override def payload: PayloadType = _internal.payload

  override def withPayload(payload: PayloadType): this.type = {
    _internal.withPayload(payload)
    this
  }

  @JSExportTopLevel("ShapeResponse")
  def this() = this(InternalShapeResponse())

}
