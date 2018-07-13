package amf.client.validation

import amf.plugins.document.webapi.validation.remote.PlatformPayloadValidator

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class PayloadValidator(private[amf] val _internal: PlatformPayloadValidator) {
  def validate(mediaType: String, payload: String): Boolean = _internal.validate(mediaType, payload)
}
