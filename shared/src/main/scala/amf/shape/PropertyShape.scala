package amf.shape

import amf.common.AMFAST
import amf.domain.{Annotations, Fields}
import amf.metadata.shape.PropertyShapeModel._

/**
  * Property shape
  */
case class PropertyShape(fields: Fields, annotations: Annotations) extends Shape {

  def path: String = fields(Path)

  def dataType: String = fields(DataType)

  def minCount: Int = fields(MinCount)

  def maxCount: Int = fields(MaxCount)
}

object PropertyShape {

  def apply(): PropertyShape = apply(Annotations())

  def apply(ast: AMFAST): PropertyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): PropertyShape = PropertyShape(Fields(), annotations)
}
