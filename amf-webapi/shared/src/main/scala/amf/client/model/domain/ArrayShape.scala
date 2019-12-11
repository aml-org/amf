package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.plugins.domain.shapes.models.{ArrayShape => InternalArrayShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ArrayShape(private[amf] override val _internal: InternalArrayShape) extends DataArrangeShape(_internal) {

  @JSExportTopLevel("model.domain.ArrayShape")
  def this() = this(InternalArrayShape())

  def items: Shape    = _internal.items
  def contains: Shape = _internal.contains

  def withItems(items: Shape): this.type = {
    _internal.withItems(items)
    this
  }
  def withContains(contains: Shape): this.type = {
    _internal.withContains(contains)
    this
  }

  override def linkCopy(): ArrayShape = ArrayShape(_internal.linkCopy())
}
