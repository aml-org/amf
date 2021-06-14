package amf.plugins.domain.apicontract.unsafe

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.validation.ValidationConfiguration
import amf.plugins.document.apicontract.validation.remote.{JsPayloadValidator, PlatformPayloadValidator}

object JsonSchemaValidatorBuilder {

  def payloadValidator(shape: Shape,
                       validationMode: ValidationMode,
                       configuration: ValidationConfiguration): PlatformPayloadValidator =
    new JsPayloadValidator(shape, validationMode, configuration)
}
