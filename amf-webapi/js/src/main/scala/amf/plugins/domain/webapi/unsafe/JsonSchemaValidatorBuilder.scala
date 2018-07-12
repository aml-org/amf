package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote.{JsJsonSchemaValidator, JsPayloadValidator, PlatformJsonSchemaValidator, PlatformPayloadValidator}
import amf.plugins.domain.shapes.models.AnyShape

object JsonSchemaValidatorBuilder {

  def apply(): PlatformJsonSchemaValidator = JsJsonSchemaValidator

  def payloadValidator(shape: AnyShape): PlatformPayloadValidator = new JsPayloadValidator(shape)
}
