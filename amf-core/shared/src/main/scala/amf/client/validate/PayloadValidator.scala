package amf.client.validate

import amf.client.model.document.PayloadFragment
import amf.core.validation.{PayloadValidator => InternalPayloadValidator}
import scala.scalajs.js.annotation.JSExportAll
import amf.client.convert.CoreClientConverters._

@JSExportAll
class PayloadValidator(private[amf] val _internal: InternalPayloadValidator) {
  def isValid(mediaType: String, payload: String): ClientFuture[Boolean] =
    _internal.isValid(mediaType, payload).asClient

  def validate(mediaType: String, payload: String): ClientFuture[ValidationReport] =
    _internal.validate(mediaType, payload).asClient
  def validate(payloadFragment: PayloadFragment): ClientFuture[ValidationReport] =
    _internal.validate(payloadFragment).asClient
}
