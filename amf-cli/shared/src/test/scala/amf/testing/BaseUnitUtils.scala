package amf.testing

import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.api.{Api, AsyncApi, WebApi}
import amf.core.client.scala.AMFResult
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{Annotation, DomainElement, Shape}
import amf.core.internal.annotations.SourceYPart
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.document.{AvroSchemaDocument, JsonSchemaDocument}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object BaseUnitUtils {
  def getApi(bu: BaseUnit, isWebApi: Boolean = true): Api =
    if (isWebApi) bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
    else bu.asInstanceOf[Document].encodes.asInstanceOf[AsyncApi]

  def getDeclarations(bu: BaseUnit): Seq[DomainElement] = bu.asInstanceOf[Document].declares

  def getReferences(bu: BaseUnit): Seq[BaseUnit] = bu.asInstanceOf[Document].references

  def getFirstDeclaration(bu: BaseUnit): DomainElement = getDeclarations(bu).head

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

  def await[R](eventualResult: Future[R]): R = {
    Await.result(eventualResult, Duration.Inf)
  }

  def sourcePartOf(unit: BaseUnit, extract: JsonSchemaDocument => Shape): String = {
    val doc             = unit.asInstanceOf[JsonSchemaDocument]
    val shapeOfInterest = extract(doc)
    val sourceYPart     = shapeOfInterest.annotations.find(_.isInstanceOf[SourceYPart]).get.asInstanceOf[SourceYPart]
    val obtainedAst     = sourceYPart.ast.toString
    obtainedAst
  }

  def hasAnnotation[T <: Annotation](annotation: Class[T], annotations: Annotations): Boolean = {
    annotations.find(annotation).isDefined
  }

  def getAvroShape(result: AMFResult): Shape = result.baseUnit.asInstanceOf[AvroSchemaDocument].encodes
}
