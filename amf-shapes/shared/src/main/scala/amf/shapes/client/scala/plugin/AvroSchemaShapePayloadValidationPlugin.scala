package amf.shapes.client.scala.plugin

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.{
  AMFShapePayloadValidationPlugin,
  AMFShapePayloadValidator,
  ShapeValidationConfiguration,
  ValidatePayloadRequest
}
import amf.core.internal.remote.Mimes._
import amf.shapes.internal.domain.apicontract.unsafe.AvroSchemaValidatorBuilder

trait AvroSchemaShapePayloadValidationPlugin extends AMFShapePayloadValidationPlugin with CommonShapeValidation {
  override val id: String                   = "AMF AVRO Payload Validation"
  private val payloadMediaType: Seq[String] = Seq(`application/json`)

  override def applies(element: ValidatePayloadRequest): Boolean = {
    val ValidatePayloadRequest(shape, mediaType, _) = element
    isAnyShape(shape) && supportsMediaType(mediaType) && isAvroSchemaShape(shape)
  }

  private def supportsMediaType(mediaType: String) = payloadMediaType.contains(mediaType)
}
object AvroSchemaShapePayloadValidationPlugin extends AvroSchemaShapePayloadValidationPlugin {

  override def validator(
      shape: Shape,
      mediaType: String,
      config: ShapeValidationConfiguration,
      validationMode: ValidationMode
  ): AMFShapePayloadValidator = {
    AvroSchemaValidatorBuilder.payloadValidator(shape, mediaType, validationMode, config)
  }
}

private[amf] object FailFastAvroSchemaPayloadValidationPlugin extends AvroSchemaShapePayloadValidationPlugin {

  override def validator(
      shape: Shape,
      mediaType: String,
      config: ShapeValidationConfiguration,
      validationMode: ValidationMode
  ): AMFShapePayloadValidator = {
    AvroSchemaValidatorBuilder.failFastValidator(shape, mediaType, validationMode, config)
  }
}
