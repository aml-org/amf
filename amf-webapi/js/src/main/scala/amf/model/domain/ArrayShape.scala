package amf.model.domain

import amf.plugins.domain.shapes.models
import amf.plugins.domain.shapes.models.DataArrangementShape

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

abstract class DataArrangeShape(private[amf] val array: DataArrangementShape) extends AnyShape(array) {

  val minItems: Int        = array.minItems
  val maxItems: Int        = array.maxItems
  val uniqueItems: Boolean = array.uniqueItems

  def withMinItems(minItems: Int): this.type = {
    array.withMinItems(minItems)
    this
  }

  def withMaxItems(maxItems: Int): this.type = {
    array.withMaxItems(maxItems)
    this
  }

  def withUniqueItems(uniqueItems: Boolean): this.type = {
    array.withUniqueItems(uniqueItems)
    this
  }
}

@JSExportAll
case class ArrayShape(private[amf] override val array: models.ArrayShape) extends DataArrangeShape(array) {
  val items: Shape = Shape(array.items)

  def withItems(items: Shape): this.type = {
    array.withItems(items.shape)
    this
  }

  override private[amf] def element = array

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.ArrayShape => ArrayShape(l) })

  override def linkCopy(): DomainElement with Linkable = ArrayShape(element.linkCopy())
}

@JSExportAll
class MatrixShape(private[amf] override val array: models.ArrayShape) extends ArrayShape(array) {

  override def withItems(items: Shape): this.type = {
    items match {
      case array: ArrayShape => super.withItems(items)
      case _                 => throw new Exception("Matrix shapes can only accept arrays as items")
    }
  }

  override private[amf] def element = array
}

object MatrixShape {
  def apply(array: models.MatrixShape): MatrixShape = { new MatrixShape(array.toArrayShape) }
}

@JSExportAll
case class TupleShape(private[amf] override val array: models.TupleShape) extends DataArrangeShape(array) {
  val items: js.Iterable[Shape] = array.items.map(Shape(_)).toJSArray

  def withItems(items: js.Iterable[Shape]): this.type = {
    array.withItems(items.map(_.shape).toSeq)
    this
  }

  override private[amf] def element = array

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.TupleShape => TupleShape(l) })

  override def linkCopy(): DomainElement with Linkable = TupleShape(element.linkCopy())
}
