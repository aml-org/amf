package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote._
import amf.plugins.domain.shapes.models.AnyShape

object JsonSchemaValidatorBuilder {

  def apply(): PlatformJsonSchemaValidator = JvmJsonSchemaValidator()

  def payloadValidator(shape: AnyShape): PlatformPayloadValidator = new JvmPayloadValidator(shape)

  def parameterValidator(shape: AnyShape): ParameterValidator = new JvmParameterValidator(shape)
}
