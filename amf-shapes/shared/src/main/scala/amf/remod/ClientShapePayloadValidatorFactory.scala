package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.model.domain.Shape
import amf.client.remod.AMFGraphConfiguration
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.client.validate.PayloadValidator
import amf.core.unsafe.PlatformSecrets

object ClientShapePayloadValidatorFactory extends PlatformSecrets {

  def createPayloadValidator(shape: Shape,
                             config: AMFGraphConfiguration = AMFGraphConfiguration.predefined()): PayloadValidator = {
    ShapePayloadValidatorFactory.createPayloadValidator(shape, new ValidationConfiguration(config))
  }

  def createParameterValidator(shape: Shape,
                               config: AMFGraphConfiguration = AMFGraphConfiguration.predefined()): PayloadValidator = {
    ShapePayloadValidatorFactory.createParameterValidator(shape, new ValidationConfiguration(config))
  }
}
