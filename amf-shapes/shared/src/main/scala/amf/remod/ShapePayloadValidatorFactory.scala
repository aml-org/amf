package amf.remod

import amf.client.plugins.{ScalarRelaxedValidationMode, StrictValidationMode, ValidationMode}
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.PayloadValidator
import amf.plugins.domain.apicontract.unsafe.JsonSchemaValidatorBuilder

object ShapePayloadValidatorFactory extends PlatformSecrets {

  def createPayloadValidator(shape: Shape, config: ValidationConfiguration): PayloadValidator = {
    validator(shape, config, StrictValidationMode)
  }

  def createParameterValidator(shape: Shape, config: ValidationConfiguration): PayloadValidator = {
    validator(shape, config, ScalarRelaxedValidationMode)
  }

  def createValidator(shape: Shape,
                      config: ValidationConfiguration,
                      validationMode: ValidationMode): PayloadValidator = {
    validator(shape, config, validationMode)
  }

  private def validator(shape: Shape, config: ValidationConfiguration, mode: ValidationMode): PayloadValidator = {
    shape match {
      case recursive: RecursiveShape =>
        recursive.fixpointTarget
          .map(target => validator(target, config, mode))
          .getOrElse(throw new Exception("Can't validate RecursiveShape with no fixpointTarget"))
      case _ => JsonSchemaValidatorBuilder.payloadValidator(shape, mode, config)
    }
  }
}
