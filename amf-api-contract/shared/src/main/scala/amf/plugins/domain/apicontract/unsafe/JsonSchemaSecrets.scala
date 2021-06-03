package amf.plugins.domain.apicontract.unsafe

import amf.client.plugins.ValidationMode
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.domain.Shape
import amf.plugins.document.apicontract.validation.remote.PlatformPayloadValidator

trait JsonSchemaSecrets {
  protected def payloadValidator(shape: Shape,
                                 config: ValidationConfiguration,
                                 validationMode: ValidationMode): PlatformPayloadValidator =
    JsonSchemaValidatorBuilder.payloadValidator(shape, validationMode, config)
}
