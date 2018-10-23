package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote._
import amf.plugins.domain.shapes.models.AnyShape

object JsonSchemaValidatorBuilder {

  def apply(): PlatformJsonSchemaValidator = JsJsonSchemaValidator

  def payloadValidator(shape: AnyShape): PlatformPayloadValidator = new JsPayloadValidator(shape)

  def parameterValidator(shape: AnyShape): ParameterValidator = new JsParameterValidator(shape)
}
