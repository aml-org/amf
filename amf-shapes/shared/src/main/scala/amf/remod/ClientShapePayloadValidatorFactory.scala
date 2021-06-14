package amf.remod

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.payload.PayloadValidator
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.validation.ValidationConfiguration

object ClientShapePayloadValidatorFactory extends PlatformSecrets {

  def createPayloadValidator(shape: Shape, config: ValidationConfiguration): PayloadValidator = {
    ShapePayloadValidatorFactory.createPayloadValidator(shape, config)
  }

  def createParameterValidator(shape: Shape, config: ValidationConfiguration): PayloadValidator = {
    ShapePayloadValidatorFactory.createParameterValidator(shape, config)
  }
}
