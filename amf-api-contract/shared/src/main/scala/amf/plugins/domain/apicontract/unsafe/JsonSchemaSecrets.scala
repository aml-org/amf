package amf.plugins.domain.apicontract.unsafe

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.validation.ValidationConfiguration
import amf.plugins.document.apicontract.validation.remote.PlatformPayloadValidator

trait JsonSchemaSecrets {
  protected def payloadValidator(shape: Shape,
                                 config: ValidationConfiguration,
                                 validationMode: ValidationMode): PlatformPayloadValidator =
    JsonSchemaValidatorBuilder.payloadValidator(shape, validationMode, config)
}
