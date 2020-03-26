package amf.plugins.document.webapi.validation.runner.steps

import amf.core.validation.{AMFValidationResult, ValidationResultProcessor}
import amf.plugins.document.webapi.validation.runner.{ResultContainer, ValidationContext}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait ValidationStep extends ValidationResultProcessor {

  val validationContext: ValidationContext

  final def run(previous: ResultContainer): Future[ResultContainer] = validate().map { nrc =>
    ResultContainer(previous.results ++ nrc)
  }

  protected def validate(): Future[Seq[AMFValidationResult]]

  def endStep: Boolean
}
