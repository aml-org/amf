package amf.plugins.document.webapi.validation.runner

import amf.core.validation._
import amf.plugins.document.webapi.validation.runner.steps._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class WebApiValidationsRunner(validationContext: ValidationContext) {

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

object EmptyResultContainer extends ResultContainer(Nil)

case class ResultContainer(results: Seq[AMFValidationResult]) {
  val errors: Boolean = results.exists(_.level == SeverityLevels.VIOLATION)
  val valid: Boolean  = !errors

}
