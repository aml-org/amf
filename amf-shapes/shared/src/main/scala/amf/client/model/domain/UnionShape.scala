package amf.client.model.domain

import amf.plugins.domain.shapes.models.{UnionShape => InternalUnionShape}
import amf.client.convert.shapeconverters.ShapeClientConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.core.client.scala.model.domain.Shape

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
