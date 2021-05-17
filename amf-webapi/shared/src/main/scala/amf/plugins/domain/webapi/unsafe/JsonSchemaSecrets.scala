package amf.plugins.domain.webapi.unsafe

import amf.client.plugins.ValidationMode
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.validation.remote.PlatformPayloadValidator

trait JsonSchemaSecrets {
  protected def payloadValidator(shape: Shape,
                                 config: ValidationConfiguration,
                                 validationMode: ValidationMode): PlatformPayloadValidator =
    JsonSchemaValidatorBuilder.payloadValidator(shape, validationMode, config)
}
