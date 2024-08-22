package amf.shapes.internal.domain.apicontract.unsafe

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.shapes.internal.document.apicontract.validation.remote.JsAvroShapePayloadValidator
import amf.shapes.internal.validation.avro.BaseAvroSchemaPayloadValidator

object AvroSchemaValidatorBuilder {

  def payloadValidator(
      shape: Shape,
      mediaType: String,
      validationMode: ValidationMode,
      configuration: ShapeValidationConfiguration
  ): BaseAvroSchemaPayloadValidator =
    new JsAvroShapePayloadValidator(shape, mediaType, validationMode, configuration)

  def failFastValidator(
      shape: Shape,
      mediaType: String,
      validationMode: ValidationMode,
      configuration: ShapeValidationConfiguration
  ): BaseAvroSchemaPayloadValidator =
    new JsAvroShapePayloadValidator(shape, mediaType, validationMode, configuration, true)
}
