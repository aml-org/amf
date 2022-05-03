package amf.shapes.client.platform.model.domain.operations

import amf.shapes.client.scala.model.domain.operations.{ShapeRequest => InternalShapeRequest}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ShapeRequest(override private[amf] val _internal: InternalShapeRequest) extends AbstractRequest {

  override type ParameterType = ShapeParameter

  override def queryParameters: ClientList[ParameterType] = _internal.queryParameters.asClient

  override def withQueryParameters(parameters: ClientList[ParameterType]): this.type = {
    _internal.withQueryParameters(parameters.asInternal)
    this
  }

  override def withQueryParameter(name: String): ParameterType = _internal.withQueryParameter(name)

  override private[amf] def buildQueryParameter: ParameterType = _internal.buildQueryParameter

  @JSExportTopLevel("ShapeRequest")
  def this() = this(InternalShapeRequest())
}
