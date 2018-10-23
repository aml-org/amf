package amf.plugins.document.webapi.validation.remote

import amf.plugins.domain.shapes.models.AnyShape

object JsJsonSchemaValidator extends PlatformJsonSchemaValidator {
  override protected def validatorForShape(s: AnyShape): PlatformPayloadValidator = new JsPayloadValidator(s)
}
