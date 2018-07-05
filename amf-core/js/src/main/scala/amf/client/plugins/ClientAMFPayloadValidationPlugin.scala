package amf.client.plugins

import amf.client.convert.CoreClientConverters._
import amf.client.model.document.PayloadFragment
import amf.client.model.domain.Shape
import amf.client.validate.{ValidationReport, ValidationShapeSet}
import amf.core.validation.{
  ValidationCandidate => InternalValidationCandidate,
  ValidationShapeSet => InternalValidationShapeSet
}

import scala.scalajs.js

@js.native
trait ClientAMFPayloadValidationPlugin extends ClientAMFPlugin {

  def parsePayload(payload: String, mediaType: String): PayloadFragment = js.native

  def validateSet(set: ValidationShapeSet): ClientFuture[ValidationReport] = js.native

  val payloadMediaType: ClientList[String] = js.native

  def canValidate(shape: Shape): Boolean = js.native
}
