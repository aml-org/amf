package amf.client.plugins

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.validation.{AMFValidationReport, ValidationShapeSet}
import amf.internal.environment.Environment

import scala.concurrent.Future

trait AMFPayloadValidationPlugin extends AMFPlugin {

  protected def parsePayload(payload: String, mediaType: String, env: Environment): PayloadFragment

  final def validatePayload(shape: Shape,
                            payload: String,
                            mediaType: String,
                            env: Environment): Future[AMFValidationReport] =
    validateSet(ValidationShapeSet(shape, parsePayload(payload, mediaType, env)), env)

  def validateSet(set: ValidationShapeSet, env: Environment): Future[AMFValidationReport]

  val payloadMediaType: Seq[String]

  def canValidate(shape: Shape, env: Environment): Boolean
}
