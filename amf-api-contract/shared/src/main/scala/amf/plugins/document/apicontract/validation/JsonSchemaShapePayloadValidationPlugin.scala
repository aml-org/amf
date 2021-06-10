package amf.plugins.document.apicontract.validation

import amf.core.client.common.validation.ValidationMode
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.{AMFShapePayloadValidationPlugin, AMFShapePayloadValidator, ValidatePayloadRequest}
import amf.core.internal.validation.ValidationConfiguration
import amf.plugins.domain.apicontract.unsafe.JsonSchemaValidatorBuilder
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}

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
