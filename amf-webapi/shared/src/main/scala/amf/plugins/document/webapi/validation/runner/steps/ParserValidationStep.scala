package amf.plugins.document.webapi.validation.runner.steps

import amf.core.errorhandling.StaticErrorCollector
import amf.core.validation.AMFValidationResult
import amf.plugins.document.webapi.validation.runner.ValidationContext

import scala.concurrent.Future

case class ParserValidationStep(override val validationContext: ValidationContext) extends ValidationStep {
  override protected def validate(): Future[Seq[AMFValidationResult]] =
    Future.successful(validationContext.baseUnit.parserRun.map(StaticErrorCollector.getRun).getOrElse(Nil))

  override def endStep: Boolean = true
}
