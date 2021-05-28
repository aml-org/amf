package amf.plugins.document.webapi.validation.remod

import amf.ProfileName
import amf.client.remod.amfcore.plugins.validate.{
  AMFValidatePlugin,
  ValidationConfiguration,
  ValidationOptions,
  ValidationResult
}
import amf.client.remod.amfcore.plugins.{HighPriority, PluginPriority}
import amf.core.model.document.BaseUnit
import amf.core.validation.AMFValidationReport
import amf.plugins.document.webapi.resolution.pipelines.ValidationTransformationPipeline
import amf.plugins.document.webapi.validation.runner.ValidationContext
import amf.plugins.document.webapi.validation.runner.steps.{
  ExamplesValidationStep,
  ModelValidationStep,
  ParserValidationStep,
  ValidationStep
}

import scala.concurrent.{ExecutionContext, Future}

trait ModelResolution {

  def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T): T = {
    if (unit.resolved) withResolved(unit, None)
    else {
      val resolvedUnit = ValidationTransformationPipeline(profile, unit, conf.eh)
      withResolved(resolvedUnit, Some(AMFValidationReport.forModel(resolvedUnit, conf.eh.getResults)))
    }
  }
}

trait LegacyContextCreation {
  def legacyContext(unit: BaseUnit, options: ValidationOptions) =
    ValidationContext(unit, options)
}

case class ValidateStepPluginAdapter(id: String, factory: ValidationContext => ValidationStep)
    extends AMFValidatePlugin
    with ModelResolution
    with LegacyContextCreation {

  override def validate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[ValidationResult] = {
    withResolvedModel(unit, options.profile, options.config) { (resolvedUnit, resolutionReport) =>
      val report = resolutionReport match {
        case Some(report) if !report.conforms => Future.successful(report)
        case _ =>
          val context = legacyContext(resolvedUnit, options)
          factory(context).run.map { validationStepReport =>
            resolutionReport.map(_.merge(validationStepReport)).getOrElse(validationStepReport)
          }
      }
      report.map(ValidationResult(resolvedUnit, _))
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
