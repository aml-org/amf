package amf.shape

import amf.common.AMFAST
import amf.domain.{Annotations, Fields}
import amf.metadata.shape.ArrayShapeModel._

/**
  * Array shape
  */
case class ArrayShape(fields: Fields, annotations: Annotations) extends Shape {

  def items: Shape         = fields(Items)
  def minItems: Int        = fields(MinItems)
  def maxItems: Int        = fields(MaxItems)
  def uniqueItems: Boolean = fields(UniqueItems)

  def withItems(items: Shape)               = set(Items, items)
  def withMinItems(minItems: Int)           = set(MinItems, minItems)
  def withMaxItems(maxItems: Int)           = set(MaxItems, maxItems)
  def withUniqueItems(uniqueItems: Boolean) = set(UniqueItems, uniqueItems)

  def withScalarItems(): ScalarShape = {
    val scalar = ScalarShape()
    this.set(Items, scalar)
    scalar
  }

  def withNodeItems(): NodeShape = {
    val node = NodeShape()
    this.set(Items, node)
    node
  }

  def withArrayItems(): ArrayShape = {
    val array = ArrayShape()
    this.set(Items, array)
    array
  }

  override def adopted(parent: String): this.type = withId(parent + "/array/" + name)
}

object ArrayShape {

  def apply(): ArrayShape = apply(Annotations())

  def apply(ast: AMFAST): ArrayShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ArrayShape = ArrayShape(Fields(), annotations)

}
