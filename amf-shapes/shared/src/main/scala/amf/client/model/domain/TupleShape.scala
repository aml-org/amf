package amf.client.model.domain

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.core.client.scala.model.BoolField
import amf.plugins.domain.shapes.models.{TupleShape => InternalTupleShape}
import amf.core.client.scala.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

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
