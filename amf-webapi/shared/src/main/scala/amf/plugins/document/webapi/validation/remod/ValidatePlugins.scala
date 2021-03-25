package amf.plugins.document.webapi.validation.remod

import amf.ProfileName
import amf.client.remod.amfcore.plugins.validate.{AMFValidatePlugin, ValidationOptions, ValidationResult}
import amf.client.remod.amfcore.plugins.{HighPriority, PluginPriority}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import amf.plugins.document.webapi.validation.runner.ValidationContext
import amf.plugins.document.webapi.validation.runner.steps.{
  ExamplesValidationStep,
  ModelValidationStep,
  ParserValidationStep,
  ValidationStep
}

import scala.concurrent.{ExecutionContext, Future}

trait ModelResolution {

  def withResolvedModel[T](unit: BaseUnit, profile: ProfileName)(withResolved: BaseUnit => T): T = {
    if (unit.resolved) withResolved(unit)
    else {
      val resolvedUnit = ValidationResolutionPipeline(profile, unit)
      withResolved(resolvedUnit)
    }
  }
}

trait LegacyContextCreation {
  def legacyContext(unit: BaseUnit, options: ValidationOptions) =
    ValidationContext(unit,
                      options.profileName,
                      messageStyle = options.profileName.messageStyle,
                      validations = options.validations,
                      env = options.environment)
}

case class ValidateStepPluginAdapter(id: String, factory: ValidationContext => ValidationStep)
    extends AMFValidatePlugin
    with ModelResolution
    with LegacyContextCreation {

  override def validate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[ValidationResult] = {
    withResolvedModel(unit, options.profileName) { resolvedUnit =>
      val context = legacyContext(resolvedUnit, options)
      factory(context).run.map(report => ValidationResult(resolvedUnit, report))
    }
  }

  override def applies(element: BaseUnit): Boolean = true

  override def priority: PluginPriority = HighPriority
}

object ValidatePlugins {
  val MODEL_PLUGIN: AMFValidatePlugin   = ValidateStepPluginAdapter("CUSTOM_SHACL_VALIDATION", ModelValidationStep)
  val PAYLOAD_PLUGIN: AMFValidatePlugin = ValidateStepPluginAdapter("PAYLOAD_VALIDATION", ExamplesValidationStep)
  val PARSER_PLUGIN: AMFValidatePlugin  = ValidateStepPluginAdapter("PARSER_VALIDATION", ParserValidationStep)
}
