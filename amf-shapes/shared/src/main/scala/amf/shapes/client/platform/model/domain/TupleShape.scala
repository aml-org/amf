package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.domain.Shape
import amf.core.client.scala.model.BoolField
import amf.shapes.internal.convert.ShapeClientConverters.ClientList

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.client.scala.domain.models.{ TupleShape => InternalTupleShape }
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class TupleShape(private[amf] override val _internal: InternalTupleShape) extends DataArrangeShape(_internal) {

  @JSExportTopLevel("model.domain.TupleShape")
  def this() = this(InternalTupleShape())

  def items: ClientList[Shape] = _internal.items.asClient

  def withItems(items: ClientList[Shape]): this.type = {
    _internal.withItems(items.asInternal)
    this
  }

  def closedItems: BoolField = _internal.closedItems

  def withClosedItems(closedItems: Boolean): this.type = {
    _internal.withClosedItems(closedItems)
    this
  }

  def additionalItemsSchema: Shape = _internal.additionalItemsSchema

  override def linkCopy(): TupleShape = _internal.linkCopy()
}
