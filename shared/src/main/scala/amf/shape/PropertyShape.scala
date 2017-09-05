package amf.shape

import amf.common.AMFAST
import amf.domain.{Annotations, Fields}
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

  override def adopted(parent: String): this.type = withId(parent + "/property/" + name)
}

object PropertyShape {

  def apply(): PropertyShape = apply(Annotations())

  def apply(ast: AMFAST): PropertyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyShape = PropertyShape(Fields(), annotations)
}
