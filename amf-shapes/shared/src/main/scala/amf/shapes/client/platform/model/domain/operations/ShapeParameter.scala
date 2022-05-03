package amf.shapes.client.platform.model.domain.operations

import amf.shapes.client.scala.model.domain.operations.{ShapeParameter => InternalShapeParameter}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ShapeParameter(override private[amf] val _internal: InternalShapeParameter)
    extends AbstractParameter(_internal) {

  @JSExportTopLevel("ShapeParameter")
  def this() = this(InternalShapeParameter())

}
