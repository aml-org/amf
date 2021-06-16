package amf.apicontract.internal.validation.payload

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.{
  AMFShapePayloadValidationPlugin,
  AMFShapePayloadValidator,
  ValidatePayloadRequest
}
import amf.core.internal.validation.ValidationConfiguration
import amf.shapes.client.scala.domain.models.SchemaShape
import amf.shapes.client.scala.model.domain.{AnyShape, SchemaShape}
import amf.shapes.internal.domain.apicontract.unsafe.JsonSchemaValidatorBuilder

object JsonSchemaShapePayloadValidationPlugin extends AMFShapePayloadValidationPlugin {

  override val id: String                   = "AMF Payload Validation"
  private val payloadMediaType: Seq[String] = Seq("application/json", "application/yaml", "text/vnd.yaml")

  override def applies(element: ValidatePayloadRequest): Boolean = {
    val ValidatePayloadRequest(shape, mediaType, _) = element
    isAnyShape(shape) && supportsMediaType(mediaType)
  }

  override def validator(shape: Shape,
                         mediaType: String,
                         config: ValidationConfiguration,
                         validationMode: ValidationMode): AMFShapePayloadValidator = {
    JsonSchemaValidatorBuilder.payloadValidator(shape, mediaType, validationMode, config)
  }

  private def isAnyShape(shape: Shape) = shape match {
    case _: SchemaShape => false
    case _: AnyShape    => true
    case _              => false
  }

  private def supportsMediaType(mediaType: String) = payloadMediaType.contains(mediaType)
}
