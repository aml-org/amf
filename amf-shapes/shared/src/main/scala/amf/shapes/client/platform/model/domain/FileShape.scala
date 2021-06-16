package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.{BoolField, DoubleField, IntField, StrField}
import amf.shapes.client.scala.model.domain
import amf.shapes.internal.convert.ShapeClientConverters.ClientList

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class FileShape(override private[amf] val _internal: domain.FileShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.FileShape")
  def this() = this(InternalFileShape())

  def fileTypes: ClientList[StrField] = _internal.fileTypes.asClient
  def pattern: StrField               = _internal.pattern
  def minLength: IntField             = _internal.minLength
  def maxLength: IntField             = _internal.maxLength
  def minimum: DoubleField            = _internal.minimum
  def maximum: DoubleField            = _internal.maximum
  def exclusiveMinimum: BoolField     = _internal.exclusiveMinimum
  def exclusiveMaximum: BoolField     = _internal.exclusiveMaximum
  def format: StrField                = _internal.format
  def multipleOf: DoubleField         = _internal.multipleOf

  def withFileTypes(fileTypes: ClientList[String]): this.type = {
    _internal.withFileTypes(fileTypes.asInternal)
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
  def withFormat(format: String): this.type = {
    _internal.withFormat(format)
    this
  }
  def withMultipleOf(multiple: Double): this.type = {
    _internal.withMultipleOf(multiple)
    this
  }

  override def linkCopy(): FileShape = _internal.linkCopy()
}
