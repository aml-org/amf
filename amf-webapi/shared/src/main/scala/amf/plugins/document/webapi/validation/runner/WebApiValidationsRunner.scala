package amf.plugins.document.webapi.validation.runner

import amf.client.execution.BaseExecutionEnvironment
import amf.core.unsafe.PlatformSecrets
import amf.core.validation._
import amf.plugins.document.webapi.validation.runner.steps._

import scala.concurrent.{ExecutionContext, Future}

case class WebApiValidationsRunner private (validationContext: ValidationContext, exec: BaseExecutionEnvironment) {

  implicit val executionContext: ExecutionContext = exec.executionContext

  private val modelStep: ValidationStep    = ModelValidationStep(validationContext)
  private val examplesStep: ValidationStep = ExamplesValidationStep(validationContext)
  private val parserStep: ValidationStep   = ParserValidationStep(validationContext)

  def runSteps: Future[AMFValidationReport] = {
    for {
      rc <- parserStep.run(EmptyResultContainer)
      rc <- if (rc.errors && parserStep.endStep) {
        Future.successful(rc)
      } else {
        modelStep.run(rc)
      }
      rc <- if (rc.errors && modelStep.endStep) {
        Future.successful(rc)
      } else examplesStep.run(rc)
    } yield {
      AMFValidationReport(rc.valid, validationContext.baseUnit.id, validationContext.profile, rc.results)
    }
  }
}

object WebApiValidationsRunner extends PlatformSecrets {
  def apply(validationContext: ValidationContext): WebApiValidationsRunner =
    new WebApiValidationsRunner(validationContext, platform.defaultExecutionEnvironment)
  def apply(validationContext: ValidationContext, exec: BaseExecutionEnvironment): WebApiValidationsRunner =
    new WebApiValidationsRunner(validationContext, exec)
}

object EmptyResultContainer extends ResultContainer(Nil)

case class ResultContainer(results: Seq[AMFValidationResult]) {
  val errors: Boolean = results.exists(_.level == SeverityLevels.VIOLATION)
  val valid: Boolean  = !errors

}
