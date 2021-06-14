package amf.client.model.domain

import amf.core.client.scala.model.domain.Shape
import amf.plugins.domain.shapes.models.{ArrayShape => InternalArrayShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ArrayShape(private[amf] override val _internal: InternalArrayShape) extends DataArrangeShape(_internal) {

  @JSExportTopLevel("model.domain.ArrayShape")
  def this() = this(InternalArrayShape())

  def items: Shape                  = _internal.items
  def contains: Shape               = _internal.contains
  def minContains: Int              = _internal.minContains
  def maxContains: Int              = _internal.maxContains
  def unevaluatedItems: Boolean     = _internal.unevaluatedItems
  def unevaluatedItemsSchema: Shape = _internal.unevaluatedItemsSchema

  def withItems(items: Shape): this.type = {
    _internal.withItems(items)
    this
  }

  def withContains(contains: Shape): this.type = {
    _internal.withContains(contains)
    this
  }

  def withMinContains(amount: Int): this.type = {
    _internal.withMinContains(amount)
    this
  }

  def withMaxContains(amount: Int): this.type = {
    _internal.withMaxContains(amount)
    this
  }

  def withUnevaluatedItemsSchema(schema: Shape): this.type = {
    _internal.withUnevaluatedItemsSchema(schema)
    this
  }

  def withUnevaluatedItems(value: Boolean): this.type = {
    _internal.withUnevaluatedItems(value)
    this
  }

  override def linkCopy(): ArrayShape = ArrayShape(_internal.linkCopy())
}
