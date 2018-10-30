package amf.client.plugins

import amf.client.convert.CoreClientConverters._
import amf.client.environment.Environment
import amf.client.model.document.PayloadFragment
import amf.client.model.domain.Shape
import amf.client.validate.ValidationReport

import scala.scalajs.js

@js.native
trait ClientAMFPayloadValidationPlugin extends ClientAMFPlugin {

  val payloadMediaType: ClientList[String] = js.native

  def canValidate(shape: Shape, env: Environment): Boolean = js.native

  def validator(s: Shape,
                env: Environment,
                validationMode: ValidationMode = StrictValidationMode): ClientPayloadValidator
}

@js.native
trait ClientPayloadValidator extends js.Object {

  val shape: Shape
  val defaultSeverity: String
  val validationMode: ValidationMode
  val env: Environment

  def validate(payload: String, mediaType: String): ValidationReport

  def validate(payloadFragment: PayloadFragment): ValidationReport

  def fastValidation(payload: String, mediaType: String): Boolean
}
