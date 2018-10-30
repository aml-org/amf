package amf.client.validate

import amf.client.model.document.PayloadFragment
import amf.core.validation.{PayloadValidator => InternalPayloadValidator}
import scala.scalajs.js.annotation.JSExportAll
import amf.client.convert.CoreClientConverters._

@JSExportAll
class PayloadValidator(private[amf] val _internal: InternalPayloadValidator) {
  def fastValidation(mediaType: String, payload: String): Boolean = _internal.fastValidation(mediaType, payload)

  def validate(mediaType: String, payload: String): ValidationReport = _internal.validate(mediaType, payload)
  def validate(payloadFragment: PayloadFragment): ValidationReport   = _internal.validate(payloadFragment)
}
