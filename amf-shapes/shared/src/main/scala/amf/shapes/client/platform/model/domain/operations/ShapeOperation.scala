package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.domain.federation.ShapeFederationMetadata
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation => InternalShapeOperation}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ShapeOperation(override private[amf] val _internal: InternalShapeOperation)
    extends AbstractOperation(_internal) {
  override type RequestType  = ShapeRequest
  override type ResponseType = ShapeResponse

  override def request: RequestType = _internal.request

  override def response: ResponseType = _internal.responses.head

  def responses: ClientList[ShapeResponse] = _internal.responses.asClient

  def federationMetadata: ShapeFederationMetadata = _internal.federationMetadata

  override private[amf] def buildResponse: ResponseType = _internal.buildResponse

  override private[amf] def buildRequest: RequestType = _internal.buildRequest

  override def withRequest(request: RequestType): this.type = {
    _internal.withRequest(request)
    this
  }

  @JSExportTopLevel("ShapeOperation")
  def this() = this(InternalShapeOperation())

  override def withResponse(name: String): ShapeResponse = {
    val result = buildResponse.withName(name)
    _internal.withResponses(Seq(result))
    result
  }

  override def withResponses(responses: ClientList[ShapeResponse]): this.type = {
    _internal.withResponses(responses.asInternal)
    this
  }

  def withFederationMetadata(metadata: ShapeFederationMetadata): this.type = {
    _internal.withFederationMetadata(metadata)
    this
  }
}
