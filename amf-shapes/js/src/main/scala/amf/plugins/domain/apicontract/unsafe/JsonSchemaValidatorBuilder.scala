package amf.plugins.domain.apicontract.unsafe

import amf.client.plugins.ValidationMode
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.domain.Shape
import amf.plugins.document.apicontract.validation.remote.{JsPayloadValidator, PlatformPayloadValidator}

object JsonSchemaValidatorBuilder {

  def payloadValidator(shape: Shape,
                       validationMode: ValidationMode,
                       configuration: ValidationConfiguration): PlatformPayloadValidator =
    new JsPayloadValidator(shape, validationMode, configuration)
}
