package amf.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class ScalarShape(private[amf] val scalar: amf.shape.ScalarShape) extends Shape(scalar) {

  val dataType: String         = scalar.dataType
  val pattern: String          = scalar.pattern
  val minLength: Int           = scalar.minLength
  val maxLength: Int           = scalar.maxLength
  val minimum: String          = scalar.minimum
  val maximum: String          = scalar.maximum
  val exclusiveMinimum: String = scalar.exclusiveMinimum
  val exclusiveMaximum: String = scalar.exclusiveMaximum
  val format: String           = scalar.format
  val multipleOf: Int          = scalar.multipleOf

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

}
