package amf.client.validation

import amf.client.convert.CoreClientConverters._
import amf.client.validate.ValidationReport
import amf.plugins.document.webapi.validation.remote.{
  PlatformPayloadValidator,
  ParameterValidator => InternalParameterValidator
}

import scala.scalajs.js.annotation.JSExportAll

trait Validator {
  def validate(mediaType: String, payload: String): Boolean

  def reportValidation(mediaType: String, payload: String): ValidationReport
}

@JSExportAll
class PayloadValidator(private[amf] val _internal: PlatformPayloadValidator) extends Validator {
  def validate(mediaType: String, payload: String): Boolean = _internal.fastValidation(mediaType, payload)

  def reportValidation(mediaType: String, payload: String): ValidationReport = _internal.validate(mediaType, payload)
}

class ParameterValidator(private[amf] val _internal: InternalParameterValidator) extends Validator {
  def validate(mediaType: String, payload: String): Boolean = _internal.fastValidation(mediaType, payload)

  def reportValidation(mediaType: String, payload: String): ValidationReport = _internal.validate(mediaType, payload)
}
