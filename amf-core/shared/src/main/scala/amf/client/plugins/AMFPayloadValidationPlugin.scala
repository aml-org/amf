package amf.client.plugins

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.validation.{AMFValidationReport, AMFValidationResult, ValidationShapeSet}
import amf.internal.environment.Environment

import scala.concurrent.Future

trait AMFPayloadValidationPlugin extends AMFPlugin {

  protected def parsePayload(payload: String, mediaType: String, env: Environment, shape: Shape): PayloadFragment

  def parsePayloadWithErrorHandler(payload: String,
                                   mediaType: String,
                                   env: Environment,
                                   shape: Shape): PayloadParsingResult

  def validateSet(set: ValidationShapeSet, env: Environment): Future[AMFValidationReport]

  val payloadMediaType: Seq[String]

  def canValidate(shape: Shape, env: Environment): Boolean
}

case class PayloadParsingResult(fragment: PayloadFragment, results: List[AMFValidationResult]) {
  def hasError: Boolean = results.nonEmpty
}
