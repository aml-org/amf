package amf.model.domain

import amf.plugins.domain.shapes.models

case class ScalarShape(private[amf] val scalar: models.ScalarShape) extends AnyShape(scalar) {

  def this() = this(models.ScalarShape())

  def dataType: String         = scalar.dataType
  def pattern: String          = scalar.pattern
  def minLength: Int           = scalar.minLength
  def maxLength: Int           = scalar.maxLength
  def minimum: String          = scalar.minimum
  def maximum: String          = scalar.maximum
  def exclusiveMinimum: String = scalar.exclusiveMinimum
  def exclusiveMaximum: String = scalar.exclusiveMaximum
  def format: String           = scalar.format
  def multipleOf: Int          = scalar.multipleOf

  def withDataType(dataType: String): this.type = {
    scalar.withDataType(dataType)
    this
  }
  def withPattern(pattern: String): this.type = {
    scalar.withPattern(pattern)
    this
  }
  def withMinLength(min: Int): this.type = {
    scalar.withMinLength(min)
    this
  }
  def withMaxLength(max: Int): this.type = {
    scalar.withMaxLength(max)
    this
  }
  def withMinimum(min: String): this.type = {
    scalar.withMinimum(min)
    this
  }
  def withMaximum(max: String): this.type = {
    scalar.withMaximum(max)
    this
  }
  def withExclusiveMinimum(min: String): this.type = {
    scalar.withExclusiveMinimum(min)
    this
  }
  def withExclusiveMaximum(max: String): this.type = {
    scalar.withExclusiveMaximum(max)
    this
  }
  def withFormat(format: String): this.type = {
    scalar.withFormat(format)
    this
  }
  def withMultipleOf(multiple: Int): this.type = {
    scalar.withMultipleOf(multiple)
    this
  }

  override private[amf] def element = scalar

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.ScalarShape => ScalarShape(l) })

  override def linkCopy(): DomainElement with Linkable = ScalarShape(element.linkCopy())
}
