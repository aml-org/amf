package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel._
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Fields}

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

  // TODO: removed to avoid dependencies @modularization
  /*
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
  */

  override def linkCopy() = PropertyShape().withId(id)

  override def meta = PropertyShapeModel

  override def cloneShape() = {
    val cloned = PropertyShape()
    cloned.id = this.id
    copyFields(cloned)
    cloned.asInstanceOf[this.type]
  }
}

object PropertyShape {
  def apply(): PropertyShape = apply(Annotations())

  def apply(annotations: Annotations): PropertyShape = PropertyShape(Fields(), annotations)
}
