package amf.plugins.domain.apicontract.unsafe

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.validation.ValidationConfiguration
import amf.plugins.document.apicontract.validation.remote.{JsShapePayloadValidator, PlatformShapePayloadValidator}

object JsonSchemaValidatorBuilder {

  def payloadValidator(shape: Shape,
                       mediaType: String,
                       validationMode: ValidationMode,
                       configuration: ValidationConfiguration): PlatformShapePayloadValidator =
    new JsShapePayloadValidator(shape, mediaType, validationMode, configuration)
}
