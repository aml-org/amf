package amf.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class ArrayShape(private[amf] val array: amf.shape.ArrayShape) extends Shape(array) {

  val items: Shape         = Shape(array.items)
  val minItems: Int        = array.minItems
  val maxItems: Int        = array.maxItems
  val uniqueItems: Boolean = array.uniqueItems

  def withItems(items: Shape): this.type = {
    array.withItems(items.shape)
    this
  }

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
