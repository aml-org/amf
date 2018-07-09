package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote.PlatformJsonSchemaValidator

trait JsonSchemaSecrets {
  val jsonSchemaValidator: PlatformJsonSchemaValidator = JsonSchemaValidatorBuilder()
}
