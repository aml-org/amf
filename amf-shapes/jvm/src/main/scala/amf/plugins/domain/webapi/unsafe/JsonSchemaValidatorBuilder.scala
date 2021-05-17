package amf.plugins.domain.webapi.unsafe

import amf.client.plugins.ValidationMode
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.validation.remote.{JvmPayloadValidator, PlatformPayloadValidator}

object JsonSchemaValidatorBuilder {

  def payloadValidator(shape: Shape,
                       validationMode: ValidationMode,
                       configuration: ValidationConfiguration): PlatformPayloadValidator =
    new JvmPayloadValidator(shape, validationMode, configuration)
}
