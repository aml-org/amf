package amf.shapes.internal.validation.plugin

import amf.core.client.common.validation._
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.{AMFValidatePlugin, ValidationInfo, ValidationOptions, ValidationResult}
import amf.core.internal.validation.{EffectiveValidations, ValidationConfiguration}

import scala.concurrent.{ExecutionContext, Future}

trait BaseModelValidationPlugin extends AMFValidatePlugin with AmlAware {

  override def priority: PluginPriority = NormalPriority

  protected def profile: ProfileName

  override def applies(element: ValidationInfo): Boolean = !isAmlUnit(element.baseUnit)

  override def validate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[ValidationResult] = {
    withResolvedModel(unit, profile, options.config) { (resolvedUnit, resolutionReport) =>
      val report = resolutionReport match {
        case Some(report) if !report.conforms => Future.successful(report)
        case _ =>
          specificValidate(resolvedUnit, options).map { validationStepReport =>
            resolutionReport.map(_.merge(validationStepReport)).getOrElse(validationStepReport)
          }
      }
      report.map(ValidationResult(resolvedUnit, _))
    }
  }

  protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport]

  protected def effectiveOrException(config: ValidationConfiguration, profile: ProfileName): EffectiveValidations = {
    config.effectiveValidations.getOrElse(profile, throw ProfileNotFoundException(profile))
  }

  protected def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T
}

case class ProfileNotFoundException(profile: ProfileName)
    extends Exception(s"No Validation Profile in configuration for ${profile.profile}")
