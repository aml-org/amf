package amf.plugins.domain.webapi.unsafe

import amf.client.plugins.ValidationMode
import amf.core.model.domain.Shape
import amf.internal.environment.Environment
import amf.plugins.document.webapi.validation.remote.{JsPayloadValidator, PlatformPayloadValidator}

object JsonSchemaValidatorBuilder {

  def payloadValidator(shape: Shape, env: Environment, validationMode: ValidationMode): PlatformPayloadValidator =
    new JsPayloadValidator(shape, validationMode, env)
}
