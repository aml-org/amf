package amf.shapes.internal.domain.apicontract.unsafe

import amf.core.client.common.validation.{StrictValidationMode, ValidationMode}
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.core.internal.remote.Mimes
import amf.shapes.internal.document.apicontract.validation.remote.JvmAvroShapePayloadValidator
import amf.shapes.internal.validation.avro.BaseAvroSchemaPayloadValidator

object AvroSchemaValidatorBuilder {

  def payloadValidator(
      shape: Shape,
      mediaType: String,
      validationMode: ValidationMode,
      configuration: ShapeValidationConfiguration
  ): BaseAvroSchemaPayloadValidator =
    new JvmAvroShapePayloadValidator(shape, mediaType, validationMode, configuration)

  def failFastValidator(
      shape: Shape,
      mediaType: String,
      validationMode: ValidationMode,
      configuration: ShapeValidationConfiguration
  ): BaseAvroSchemaPayloadValidator =
    new JvmAvroShapePayloadValidator(shape, mediaType, validationMode, configuration)

  // used for validating parsed shape in parsing
  def validateSchema(
      schema: Shape,
      mediaType: String = Mimes.`application/json`,
      validationMode: ValidationMode = StrictValidationMode,
      configuration: ShapeValidationConfiguration = ShapeValidationConfiguration.predefined()
  ): Seq[AMFValidationResult] = {
    new JvmAvroShapePayloadValidator(schema, mediaType, validationMode, configuration).validateAvroSchema()
  }
}
