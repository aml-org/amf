package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote.{JvmJsonSchemaValidator, JvmPayloadValidator, PlatformJsonSchemaValidator, PlatformPayloadValidator}
import amf.plugins.domain.shapes.models.AnyShape

object JsonSchemaValidatorBuilder {

  def apply(): PlatformJsonSchemaValidator = JvmJsonSchemaValidator

  def payloadValidator(shape: AnyShape): PlatformPayloadValidator = new JvmPayloadValidator(shape)
}
