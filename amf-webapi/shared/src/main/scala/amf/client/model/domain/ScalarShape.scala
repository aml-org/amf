package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.{IntField, StrField}
import amf.plugins.domain.shapes.models.{ScalarShape => InternalScalarShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ScalarShape(override private[amf] val _internal: InternalScalarShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.ScalarShape")
  def this() = this(InternalScalarShape())

  def dataType: StrField         = _internal.dataType
  def pattern: StrField          = _internal.pattern
  def minLength: IntField        = _internal.minLength
  def maxLength: IntField        = _internal.maxLength
  def minimum: StrField          = _internal.minimum
  def maximum: StrField          = _internal.maximum
  def exclusiveMinimum: StrField = _internal.exclusiveMinimum
  def exclusiveMaximum: StrField = _internal.exclusiveMaximum
  def format: StrField           = _internal.format
  def multipleOf: IntField       = _internal.multipleOf

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
  def withMinimum(min: String): this.type = {
    _internal.withMinimum(min)
    this
  }
  def withMaximum(max: String): this.type = {
    _internal.withMaximum(max)
    this
  }
  def withExclusiveMinimum(min: String): this.type = {
    _internal.withExclusiveMinimum(min)
    this
  }
  def withExclusiveMaximum(max: String): this.type = {
    _internal.withExclusiveMaximum(max)
    this
  }
  def withFormat(format: String): this.type = {
    _internal.withFormat(format)
    this
  }
  def withMultipleOf(multiple: Int): this.type = {
    _internal.withMultipleOf(multiple)
    this
  }

  override def linkTarget: Option[DomainElement] =
    _internal.linkTarget.map({ case l: InternalScalarShape => l }).asClient

  override def linkCopy(): ScalarShape = _internal.linkCopy()
}
