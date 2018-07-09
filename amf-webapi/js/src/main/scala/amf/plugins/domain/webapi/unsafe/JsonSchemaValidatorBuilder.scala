package amf.plugins.domain.webapi.unsafe

import amf.plugins.document.webapi.validation.remote.{JsJsonSchemaValidator, PlatformJsonSchemaValidator}

object JsonSchemaValidatorBuilder {

  def apply(): PlatformJsonSchemaValidator = JsJsonSchemaValidator

}
