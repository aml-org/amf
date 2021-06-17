package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.domain.Shape
import amf.shapes.internal.convert.ShapeClientConverters.ClientList

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._
import amf.shapes.client.scala.model.domain.{UnionShape => InternalUnionShape}

@JSExportAll
case class UnionShape(override private[amf] val _internal: InternalUnionShape) extends AnyShape(_internal) {

  @JSExportTopLevel("UnionShape")
  def this() = this(InternalUnionShape())

  def anyOf: ClientList[Shape] = _internal.anyOf.asClient
  def serializationSchema: Shape   = _internal.serializationSchema

  def withAnyOf(anyOf: ClientList[Shape]): UnionShape = {
    _internal.withAnyOf(anyOf.asInternal)
    this
  }

  def withSerializationSchema(schema: Shape): this.type = {
    _internal.withSerializationSchema(schema)
    this
  }

}
