package amf.plugins.domain.webapi.unsafe

import amf.client.plugins.ValidationMode
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.validation.remote._

object JsonSchemaValidatorBuilder {

  def payloadValidator(shape: Shape, validationMode: ValidationMode): PlatformPayloadValidator =
    new JvmPayloadValidator(shape, validationMode)
}
