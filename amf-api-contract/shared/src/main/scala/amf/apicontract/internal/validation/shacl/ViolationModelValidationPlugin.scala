package amf.apicontract.internal.validation.shacl

import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin
import amf.core.client.common.validation.{ProfileName, ProfileNames, SeverityLevels, UnknownProfile}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.internal.plugins.validation.{ValidationOptions, ValidationResult}

import scala.concurrent.{ExecutionContext, Future}

case class ViolationModelValidationPlugin(configName: String) extends BaseApiValidationPlugin {
  override val id: String = "Violation Plugin"

  override def priority: PluginPriority = NormalPriority

  override protected def profile: ProfileName = ProfileNames.AMF

  override def validate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[ValidationResult] = {
    specificValidate(unit, options).map { report =>
      ValidationResult(unit, report)
    }
  }

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    Future.successful { AMFValidationReport(unit.id, UnknownProfile, Seq(validationResult)) }
  }

  private def validationResult: AMFValidationResult = AMFValidationResult(
    s"Model validation is not supported for the $configName composite configuration",
    SeverityLevels.VIOLATION,
    "",
    None,
    "",
    None,
    None,
    ""
  )
}
