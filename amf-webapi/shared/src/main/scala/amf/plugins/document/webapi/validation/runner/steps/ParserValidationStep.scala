package amf.plugins.document.webapi.validation.runner.steps

import amf.core.errorhandling.StaticErrorCollector
import amf.core.validation.{AMFValidationReport, AMFValidationResult}
import amf.plugins.document.webapi.validation.runner.ValidationContext

import scala.concurrent.{ExecutionContext, Future}

case class ParserValidationStep(override val validationContext: ValidationContext) extends ValidationStep {
  override protected def validate()(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val results = validationContext.baseUnit.parserRun.map(StaticErrorCollector.getRun).getOrElse(Nil)
    val report  = AMFValidationReport(validationContext.baseUnit.id, validationContext.profile, results)
    Future.successful(report)
  }
}
