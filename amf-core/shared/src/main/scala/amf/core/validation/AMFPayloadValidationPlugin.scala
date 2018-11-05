package amf.core.validation
import amf.client.plugins.{AMFPlugin, StrictValidationMode, ValidationMode}
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.internal.environment.Environment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AMFPayloadValidationPlugin extends AMFPlugin {

  val payloadMediaType: Seq[String]

  def canValidate(shape: Shape, env: Environment): Boolean

  def validator(s: Shape, env: Environment, validationMode: ValidationMode = StrictValidationMode): PayloadValidator

}

trait PayloadValidator {

  val shape: Shape
  val defaultSeverity: String
  val validationMode: ValidationMode
  val env: Environment

  def validate(mediaType: String, payload: String): Future[AMFValidationReport]

  def validate(payloadFragment: PayloadFragment): Future[AMFValidationReport]

  def isValid(mediaType: String, payload: String): Future[Boolean]

}

case class PayloadParsingResult(fragment: PayloadFragment, results: List[AMFValidationResult]) {
  def hasError: Boolean = results.nonEmpty
}
