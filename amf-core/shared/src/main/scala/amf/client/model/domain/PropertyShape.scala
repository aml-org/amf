package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.client.model.{BoolField, IntField, StrField}
import amf.core.model.domain.extensions.{PropertyShape => InternalPropertyShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("model.domain.PropertyShape")
case class PropertyShape(override private[amf] val _internal: InternalPropertyShape) extends DomainElement {

  @JSExportTopLevel("model.domain.PropertyShape")
  def this() = this(InternalPropertyShape())

  def path: StrField      = _internal.path
  def range: Shape        = _internal.range
  def minCount: IntField  = _internal.minCount
  def maxCount: IntField  = _internal.maxCount
  def readOnly: BoolField = _internal.readOnly

  def withPath(path: String): this.type = {
    _internal.withPath(path)
    this
  }

  def withRange(range: Shape): this.type = {
    _internal.withRange(range._internal)
    this
  }

  def withMinCount(min: Int): this.type = {
    _internal.withMinCount(min)
    this
  }
  def withMaxCount(max: Int): this.type = {
    _internal.withMaxCount(max)
    this
  }

  def withReadOnly(readOnly: Boolean): this.type = {
    _internal.withReadOnly(readOnly)
    this
  }
}
