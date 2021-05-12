package amf.remod

import amf.client.plugins.{ScalarRelaxedValidationMode, StrictValidationMode, ValidationMode}
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.PayloadValidator
import amf.internal.environment.Environment
import amf.plugins.domain.webapi.unsafe.JsonSchemaValidatorBuilder

object ShapePayloadValidatorFactory extends PlatformSecrets {

  def createPayloadValidator(
      shape: Shape,
      env: Environment = Environment(platform.defaultExecutionEnvironment)): PayloadValidator = {
    validator(shape, env, StrictValidationMode)
  }

  def createParameterValidator(
      shape: Shape,
      env: Environment = Environment(platform.defaultExecutionEnvironment)): PayloadValidator = {
    validator(shape, env, ScalarRelaxedValidationMode)
  }

  def createValidator(shape: Shape,
                      env: Environment = Environment(platform.defaultExecutionEnvironment),
                      validationMode: ValidationMode): PayloadValidator = {
    validator(shape, env, validationMode)
  }

  private def validator(shape: Shape,
                        env: Environment = Environment(platform.defaultExecutionEnvironment),
                        mode: ValidationMode): PayloadValidator = {
    shape match {
      case recursive: RecursiveShape =>
        recursive.fixpointTarget
          .map(target => validator(target, env, mode))
          .getOrElse(throw new Exception("Can't validate RecursiveShape with no fixpointTarget"))
      case _ => JsonSchemaValidatorBuilder.payloadValidator(shape, env, mode)
    }
  }
}
