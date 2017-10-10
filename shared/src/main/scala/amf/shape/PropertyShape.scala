package amf.shape

import amf.domain.{Annotations, Fields}
import amf.metadata.shape.PropertyShapeModel
import amf.metadata.shape.PropertyShapeModel._

/**
  * Property shape
  */
case class PropertyShape(fields: Fields, annotations: Annotations) extends Shape {

  def path: String  = fields(Path)
  def range: Shape  = fields(Range)
  def minCount: Int = fields(MinCount)
  def maxCount: Int = fields(MaxCount)

  def withPath(path: String): this.type  = set(Path, path)
  def withRange(range: Shape): this.type = set(Range, range)

  def withMinCount(min: Int): this.type  = set(MinCount, min)
  def withMaxCount(max: Int): this.type  = set(MaxCount, max)

  override def adopted(parent: String): this.type = {
    withId(parent + "/property/" + name)
    if (Option(range).isDefined) {
      range.adopted(id)
    }
    this
  }

  def withObjectRange(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(PropertyShapeModel.Range, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(PropertyShapeModel.Range, scalar)
    scalar
  }

  override def linkCopy() = PropertyShape().withId(id)
}

object PropertyShape {
  def apply(): PropertyShape = apply(Annotations())

  def apply(annotations: Annotations): PropertyShape = PropertyShape(Fields(), annotations)
}
