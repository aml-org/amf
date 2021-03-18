package amf.plugins.document.webapi.validation.runner.steps

import amf.core.validation.AMFValidationReport
import amf.plugins.document.webapi.validation.runner.ValidationContext

import scala.concurrent.{ExecutionContext, Future}

trait ValidationStep {

  val validationContext: ValidationContext

  final def run()(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = validate()

  protected def validate()(implicit executionContext: ExecutionContext): Future[AMFValidationReport]
}
