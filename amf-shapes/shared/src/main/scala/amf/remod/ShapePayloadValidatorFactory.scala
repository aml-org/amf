package amf.remod

import amf.client.plugins.{ScalarRelaxedValidationMode, StrictValidationMode, ValidationMode}
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.registries.AMFPluginsRegistry
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.{AMFPayloadValidationPlugin, PayloadValidator, SeverityLevels, ValidationCandidate}
import amf.internal.environment.Environment
import amf.plugins.domain.webapi.unsafe.JsonSchemaValidatorBuilder

object ShapePayloadValidatorFactory extends PlatformSecrets with PayloadValidationPluginFinder {

  def createPayloadValidator(
      shape: Shape,
      mediaType: String,
      env: Environment = Environment(platform.defaultExecutionEnvironment)): PayloadValidator = {
    validator(shape, mediaType, env, StrictValidationMode)
  }

  def createParameterValidator(
      shape: Shape,
      mediaType: String,
      env: Environment = Environment(platform.defaultExecutionEnvironment)): PayloadValidator = {
    validator(shape, mediaType, env, ScalarRelaxedValidationMode)
  }

  def createValidator(shape: Shape,
                      mediaType: String,
                      env: Environment = Environment(platform.defaultExecutionEnvironment),
                      validationMode: ValidationMode): PayloadValidator = {
    validator(shape, mediaType, env, validationMode)
  }

  private def validator(shape: Shape,
                        mediaType: String,
                        env: Environment = Environment(platform.defaultExecutionEnvironment),
                        mode: ValidationMode): PayloadValidator = {
    shape match {
      case recursive: RecursiveShape =>
        recursive.fixpointTarget
          .map(target => validator(target, mediaType, env, mode))
          .getOrElse(throw new Exception("Can't validate RecursiveShape with no fixpointTarget"))
      case _ =>
        searchPlugin(mediaType, shape, env)
          .getOrElse(AnyMatchPayloadPlugin(SeverityLevels.VIOLATION))
          .validator(shape, mediaType, env, mode)
    }
  }
}
