package amf.plugins.document.webapi.validation.plugins

import amf.{
  AmfProfile,
  Async20Profile,
  AsyncProfile,
  Oas20Profile,
  Oas30Profile,
  ProfileName,
  Raml08Profile,
  Raml10Profile
}
import amf.client.remod.amfcore.plugins.{HighPriority, PluginPriority}
import amf.client.remod.amfcore.plugins.validate.{
  AMFValidatePlugin,
  ValidationConfiguration,
  ValidationInfo,
  ValidationOptions,
  ValidationResult
}
import amf.core.model.document.BaseUnit
import amf.core.validation.AMFValidationReport

import scala.concurrent.{ExecutionContext, Future}

object BaseApiValidationPlugin {
  val standardApiProfiles =
    Seq(Raml08Profile, Raml10Profile, Oas20Profile, Oas30Profile, Async20Profile, AsyncProfile, AmfProfile)
}

trait BaseApiValidationPlugin extends AMFValidatePlugin with ModelResolution with AmlAware {

  override def priority: PluginPriority = HighPriority

  override def applies(element: ValidationInfo): Boolean = !isAmlUnit(element.baseUnit)

  override def validate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[ValidationResult] = {
    withResolvedModel(unit, options.profile, options.config) { (resolvedUnit, resolutionReport) =>
      val report = resolutionReport match {
        case Some(report) if !report.conforms => Future.successful(report)
        case _ =>
          specificValidate(resolvedUnit, options.profile, options).map { validationStepReport =>
            resolutionReport.map(_.merge(validationStepReport)).getOrElse(validationStepReport)
          }
      }
      report.map(ValidationResult(resolvedUnit, _))
    }
  }

  protected def specificValidate(unit: BaseUnit, profile: ProfileName, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport]
}
