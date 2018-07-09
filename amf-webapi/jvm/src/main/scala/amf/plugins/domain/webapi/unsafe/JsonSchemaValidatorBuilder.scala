package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote.{JvmJsonSchemaValidator, PlatformJsonSchemaValidator}

object JsonSchemaValidatorBuilder {

  def apply(): PlatformJsonSchemaValidator = JvmJsonSchemaValidator

}
