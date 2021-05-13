package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.environment.Environment
import amf.client.model.domain.Shape
import amf.client.validate.PayloadValidator
import amf.core.unsafe.PlatformSecrets

object ClientShapePayloadValidatorFactory extends PlatformSecrets {

  def createPayloadValidator(
      shape: Shape,
      mediaType: String,
      env: Environment = Environment.empty(platform.defaultExecutionEnvironment)): PayloadValidator = {
    ShapePayloadValidatorFactory.createPayloadValidator(shape, mediaType, env._internal)
  }

  def createParameterValidator(
      shape: Shape,
      mediaType: String,
      env: Environment = Environment.empty(platform.defaultExecutionEnvironment)): PayloadValidator = {
    ShapePayloadValidatorFactory.createParameterValidator(shape, mediaType, env._internal)
  }
}
