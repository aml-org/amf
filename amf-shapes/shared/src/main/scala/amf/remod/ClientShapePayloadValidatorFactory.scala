package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.environment.Environment
import amf.client.model.domain.Shape
import amf.client.validate.PayloadValidator
import amf.core.unsafe.PlatformSecrets

object ClientShapePayloadValidatorFactory extends PlatformSecrets {

  def createPayloadValidator(
      shape: Shape,
      env: Environment = Environment.empty(platform.defaultExecutionEnvironment)): PayloadValidator = {
    ShapePayloadValidatorFactory.createPayloadValidator(shape, env._internal)
  }

  def createParameterValidator(
      shape: Shape,
      env: Environment = Environment.empty(platform.defaultExecutionEnvironment)): PayloadValidator = {
    ShapePayloadValidatorFactory.createParameterValidator(shape, env._internal)
  }
}
