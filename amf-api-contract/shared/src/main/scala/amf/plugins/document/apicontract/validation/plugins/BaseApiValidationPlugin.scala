package amf.plugins.document.apicontract.validation.plugins

import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.common.validation._
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.{AMFValidatePlugin, ValidationInfo, ValidationOptions, ValidationResult}

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
