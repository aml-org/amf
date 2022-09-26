package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.{BoolField, DoubleField, IntField, StrField}
import amf.shapes.internal.domain.metamodel.ScalarShapeModel._

protected[amf] trait CommonShapeFields { self: Shape =>

  def pattern: StrField                    = fields.field(Pattern)
  def minLength: IntField                  = fields.field(MinLength)
  def maxLength: IntField                  = fields.field(MaxLength)
  def minimum: DoubleField                 = fields.field(Minimum)
  def maximum: DoubleField                 = fields.field(Maximum)
  def exclusiveMinimum: BoolField          = fields.field(ExclusiveMinimum)
  def exclusiveMaximum: BoolField          = fields.field(ExclusiveMaximum)
  def exclusiveMinimumNumeric: DoubleField = fields.field(ExclusiveMinimumNumeric)
  def exclusiveMaximumNumeric: DoubleField = fields.field(ExclusiveMaximumNumeric)
  def format: StrField                     = fields.field(Format)
  def multipleOf: DoubleField              = fields.field(MultipleOf)

  def withPattern(pattern: String): this.type             = set(Pattern, pattern)
  def withMinLength(min: Int): this.type                  = set(MinLength, min)
  def withMaxLength(max: Int): this.type                  = set(MaxLength, max)
  def withMinimum(min: Double): this.type                 = set(Minimum, min)
  def withMaximum(max: Double): this.type                 = set(Maximum, max)
  def withExclusiveMinimum(min: Boolean): this.type       = set(ExclusiveMinimum, min)
  def withExclusiveMaximum(max: Boolean): this.type       = set(ExclusiveMaximum, max)
  def withExclusiveMinimumNumeric(min: Double): this.type = set(ExclusiveMinimumNumeric, min)
  def withExclusiveMaximumNumeric(max: Double): this.type = set(ExclusiveMaximumNumeric, max)
  def withFormat(format: String): this.type               = set(Format, format)
  def withMultipleOf(multiple: Double): this.type         = set(MultipleOf, multiple)

}
