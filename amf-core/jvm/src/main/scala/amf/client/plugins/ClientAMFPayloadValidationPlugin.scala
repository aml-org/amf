package amf.client.plugins

import amf.client.convert.CoreClientConverters._
import amf.client.environment.Environment
import amf.client.model.document.PayloadFragment
import amf.client.model.domain.Shape
import amf.client.validate.ValidationReport

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait ClientAMFPayloadValidationPlugin extends ClientAMFPlugin {

  val payloadMediaType: ClientList[String]

  def canValidate(shape: Shape, env: Environment): Boolean

  def validator(s: Shape,
                env: Environment,
                validationMode: ValidationMode = StrictValidationMode): ClientPayloadValidator
}

trait ClientPayloadValidator {

  val shape: Shape
  val defaultSeverity: String
  val validationMode: ValidationMode
  val env: Environment

  def validate(payload: String, mediaType: String): ValidationReport

  def validate(payloadFragment: PayloadFragment): ValidationReport

  def isValid(payload: String, mediaType: String): Boolean
}
