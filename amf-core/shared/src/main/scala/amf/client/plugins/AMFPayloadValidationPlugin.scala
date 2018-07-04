package amf.client.plugins

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.validation.{AMFValidationReport, ValidationShapeSet}

import scala.concurrent.Future
@JSE
trait AMFPayloadValidationPlugin extends AMFPlugin {

  protected def parsePayload(payload: String, mediaType: String): PayloadFragment

  final def validatePayload(shape: Shape, payload: String, mediaType: String): Future[AMFValidationReport] =
    validateSet(ValidationShapeSet(shape, parsePayload(payload, mediaType)))

  def validateSet(set: ValidationShapeSet): Future[AMFValidationReport]

  val payloadMediaType: Seq[String]

  def canValidate(shape: Shape): Boolean
}
