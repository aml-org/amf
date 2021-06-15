package amf.client.model.domain

import amf.plugins.domain.shapes.models.{UnionShape => InternalUnionShape}
import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.core.client.platform.model.domain.Shape

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class UnionShape(override private[amf] val _internal: InternalUnionShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.UnionShape")
  def this() = this(InternalUnionShape())

  def anyOf: ClientList[Shape] = _internal.anyOf.asClient

  def withAnyOf(anyOf: ClientList[Shape]): UnionShape = {
    _internal.withAnyOf(anyOf.asInternal)
    this
  }
}
