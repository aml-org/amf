package amf.core.model.domain.extensions

import amf.core.metamodel.domain.ShapeModel
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

  def withMinCount(min: Int): this.type = set(MinCount, min)
  def withMaxCount(max: Int): this.type = set(MaxCount, max)

  override def adopted(parent: String): this.type = {
    withId(parent + "/property/" + name)
    if (Option(range).isDefined) {
      range.adopted(id)
    }
    this
  }

  override def linkCopy(): PropertyShape = PropertyShape().withId(id)

  override def meta: ShapeModel = PropertyShapeModel

  override def cloneShape(withRecursionBase: Option[String], traversed: Set[String]): PropertyShape = {
    val cloned = PropertyShape()
    cloned.id = this.id
    copyFields(cloned, withRecursionBase, traversed)
    cloned.asInstanceOf[this.type]
  }
}

object PropertyShape {
  def apply(): PropertyShape = apply(Annotations())

  def apply(annotations: Annotations): PropertyShape = PropertyShape(Fields(), annotations)
}
