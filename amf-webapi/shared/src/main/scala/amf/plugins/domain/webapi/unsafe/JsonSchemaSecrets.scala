package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote.{
  ParameterValidator,
  PlatformJsonSchemaValidator,
  PlatformPayloadValidator
}
import amf.plugins.domain.shapes.models.AnyShape

trait JsonSchemaSecrets {
  protected val jsonSchemaValidator: PlatformJsonSchemaValidator = JsonSchemaValidatorBuilder()
  protected def payloadValidator(shape: AnyShape): PlatformPayloadValidator =
    JsonSchemaValidatorBuilder.payloadValidator(shape)
  protected def parameterValidator(shape: AnyShape): ParameterValidator =
    JsonSchemaValidatorBuilder.parameterValidator(shape)
}
