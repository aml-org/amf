package amf.shape

import amf.domain.{Annotations, Fields}
import amf.metadata.shape.ScalarShapeModel._
import org.yaml.model.YPart

/**
  * Scalar shape
  */
case class ScalarShape(fields: Fields, annotations: Annotations) extends Shape {

  def dataType: String         = fields(DataType)
  def pattern: String          = fields(Pattern)
  def minLength: Int           = fields(MinLength)
  def maxLength: Int           = fields(MaxLength)
  def minimum: String          = fields(Minimum)
  def maximum: String          = fields(Maximum)
  def exclusiveMinimum: String = fields(ExclusiveMinimum)
  def exclusiveMaximum: String = fields(ExclusiveMaximum)
  def format: String           = fields(Format)
  def multipleOf: Int          = fields(MultipleOf)

  def withDataType(dataType: String): this.type    = set(DataType, dataType)
  def withPattern(pattern: String): this.type      = set(Pattern, pattern)
  def withMinLength(min: Int): this.type           = set(MinLength, min)
  def withMaxLength(max: Int): this.type           = set(MaxLength, max)
  def withMinimum(min: String): this.type          = set(Minimum, min)
  def withMaximum(max: String): this.type          = set(Maximum, max)
  def withExclusiveMinimum(min: String): this.type = set(ExclusiveMinimum, min)
  def withExclusiveMaximum(max: String): this.type = set(ExclusiveMaximum, max)
  def withFormat(format: String): this.type        = set(Format, format)
  def withMultipleOf(multiple: Int): this.type     = set(MultipleOf, multiple)

  override def adopted(parent: String): this.type = withId(parent + "/scalar/" + name)

  override def linkCopy() = PropertyShape().withId(id)
}

object ScalarShape {

  def apply(): ScalarShape = apply(Annotations())

  def apply(ast: YPart): ScalarShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ScalarShape = ScalarShape(Fields(), annotations)
}
