package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.model.domain.Shape
import amf.client.remod.AMFGraphConfiguration
import amf.client.remod.amfcore.plugins.validate.{ValidationConfiguration, ValidationInfo}
import amf.client.validate.PayloadValidator
import amf.core.unsafe.PlatformSecrets

object ClientShapePayloadValidatorFactory extends PlatformSecrets {

  def createPayloadValidator(shape: Shape, config: ValidationConfiguration): PayloadValidator = {
    ShapePayloadValidatorFactory.createPayloadValidator(shape, config)
  }

  def createParameterValidator(shape: Shape, config: ValidationConfiguration): PayloadValidator = {
    ShapePayloadValidatorFactory.createParameterValidator(shape, config)
  }
}
