package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.domain.Shape
import amf.core.client.platform.model.{BoolField, DoubleField, IntField, StrField}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._
import amf.shapes.client.scala.model.domain.{ScalarShape => InternalScalarShape}

@JSExportAll
case class ScalarShape(override private[amf] val _internal: InternalScalarShape) extends AnyShape(_internal) {

  @JSExportTopLevel("ScalarShape")
  def this() = this(InternalScalarShape())

  def dataType: StrField                   = _internal.dataType
  def pattern: StrField                    = _internal.pattern
  def minLength: IntField                  = _internal.minLength
  def maxLength: IntField                  = _internal.maxLength
  def minimum: DoubleField                 = _internal.minimum
  def maximum: DoubleField                 = _internal.maximum
  def exclusiveMinimum: BoolField          = _internal.exclusiveMinimum
  def exclusiveMaximum: BoolField          = _internal.exclusiveMaximum
  def exclusiveMinimumNumeric: DoubleField = _internal.exclusiveMinimumNumeric
  def exclusiveMaximumNumeric: DoubleField = _internal.exclusiveMaximumNumeric
  def format: StrField                     = _internal.format
  def multipleOf: DoubleField              = _internal.multipleOf
  def encoding: StrField                   = _internal.encoding
  def mediaType: StrField                  = _internal.mediaType
  def schema: Shape                        = _internal.schema

  def withDataType(dataType: String): this.type = {
    _internal.withDataType(dataType)
    this
  }
  def withPattern(pattern: String): this.type = {
    _internal.withPattern(pattern)
    this
  }
  def withMinLength(min: Int): this.type = {
    _internal.withMinLength(min)
    this
  }
  def withMaxLength(max: Int): this.type = {
    _internal.withMaxLength(max)
    this
  }
  def withMinimum(min: Double): this.type = {
    _internal.withMinimum(min)
    this
  }
  def withMaximum(max: Double): this.type = {
    _internal.withMaximum(max)
    this
  }
  def withExclusiveMinimum(min: Boolean): this.type = {
    _internal.withExclusiveMinimum(min)
    this
  }
  def withExclusiveMaximum(max: Boolean): this.type = {
    _internal.withExclusiveMaximum(max)
    this
  }
  def withExclusiveMinimumNumeric(min: Double): this.type = {
    _internal.withExclusiveMinimumNumeric(min)
    this
  }
  def withExclusiveMaximumNumeric(max: Double): this.type = {
    _internal.withExclusiveMaximumNumeric(max)
    this
  }
  def withFormat(format: String): this.type = {
    _internal.withFormat(format)
    this
  }
  def withMultipleOf(multiple: Double): this.type = {
    _internal.withMultipleOf(multiple)
    this
  }
  def withEncoding(encoding: String): this.type = {
    _internal.withEncoding(encoding)
    this
  }
  def withMediaType(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }
  def withSchema(schema: Shape): this.type = {
    _internal.withSchema(schema)
    this
  }

  def withSerializationSchema(schema: Shape): this.type = {
    _internal.withSerializationSchema(schema)
    this
  }

  override def linkCopy(): ScalarShape = _internal.linkCopy()
}
