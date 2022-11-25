package amf.testing

import amf.apicontract.client.scala.model.domain.api.{Api, AsyncApi, WebApi}
import amf.apicontract.client.scala.model.domain._
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Shape

object BaseUnitUtils {
  def getApi(bu: BaseUnit, isWebApi: Boolean = true): Api =
    if (isWebApi) bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
    else bu.asInstanceOf[Document].encodes.asInstanceOf[AsyncApi]

  def getEndpoints(bu: BaseUnit, isWebApi: Boolean = true): Seq[EndPoint] = getApi(bu, isWebApi).endPoints

  def getServers(bu: BaseUnit, isWebApi: Boolean = true): Seq[Server] = getApi(bu, isWebApi).servers

  def getFirstEndpoint(bu: BaseUnit, isWebApi: Boolean = true): EndPoint = getApi(bu, isWebApi).endPoints.head

  def getFirstOperation(bu: BaseUnit, isWebApi: Boolean = true): Operation =
    getFirstEndpoint(bu, isWebApi).operations.head

  def getFirstRequest(bu: BaseUnit, isWebApi: Boolean = true): Request = getFirstOperation(bu, isWebApi).requests.head

  def getFirstResponse(bu: BaseUnit, isWebApi: Boolean = true): Response =
    getFirstOperation(bu, isWebApi).responses.head

  def getFirstRequestPayload(bu: BaseUnit, isWebApi: Boolean = true): Payload =
    getFirstRequest(bu, isWebApi).payloads.head

  def getFirstResponsePayload(bu: BaseUnit, isWebApi: Boolean = true): Payload =
    getFirstResponse(bu, isWebApi).payloads.head

  def getFirstPayloadSchema(bu: BaseUnit, isWebApi: Boolean = true): Shape =
    getFirstResponsePayload(bu, isWebApi).schema
}
