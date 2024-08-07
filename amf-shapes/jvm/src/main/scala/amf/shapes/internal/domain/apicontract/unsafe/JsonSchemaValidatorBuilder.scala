package amf.shapes.internal.domain.apicontract.unsafe

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.shapes.internal.document.apicontract.validation.remote.JvmShapePayloadValidator
import amf.shapes.internal.validation.jsonschema.BaseJsonSchemaPayloadValidator

object JsonSchemaValidatorBuilder {

  def payloadValidator(
      shape: Shape,
      mediaType: String,
      validationMode: ValidationMode,
      configuration: ShapeValidationConfiguration
  ): BaseJsonSchemaPayloadValidator =
    new JvmShapePayloadValidator(shape, mediaType, validationMode, configuration)

  def failFastValidator(
      shape: Shape,
      mediaType: String,
      validationMode: ValidationMode,
      configuration: ShapeValidationConfiguration
  ): BaseJsonSchemaPayloadValidator =
    new JvmShapePayloadValidator(shape, mediaType, validationMode, configuration, true)
}
