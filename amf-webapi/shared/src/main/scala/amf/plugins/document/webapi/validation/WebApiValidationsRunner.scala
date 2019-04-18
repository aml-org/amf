package amf.plugins.document.webapi.validation

import amf._
import amf.core.benchmark.ExecutionLog
import amf.core.metamodel.Field
import amf.core.metamodel.domain.DataNodeModel
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.{RuntimeValidator, ValidationOptions}
import amf.core.validation._
import amf.core.validation.core.ValidationResult
import amf.internal.environment.Environment
import amf.validations.CustomShaclFunctions

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
trait ValidationStep extends ValidationResultProcessor {

  val validationContext: ValidationContext

  final def run(previous: ResultContainer): Future[ResultContainer] = validate().map { nrc =>
    ResultContainer(previous.results ++ nrc)
  }
  protected def validate(): Future[Seq[AMFValidationResult]]

  def endStep: Boolean
}

case class FilterDataNodeOptions() extends ValidationOptions {
  override val filterFields: Field => Boolean = (f: Field) => f.`type` == DataNodeModel
}

case class ModelValidationStep(override val validationContext: ValidationContext) extends ValidationStep {

  override protected def validate(): Future[Seq[AMFValidationResult]] = {
    val baseOptions = FilterDataNodeOptions().withMessageStyle(validationContext.messageStyle)
    val options = validationContext.profile match {
      case RamlProfile | Raml10Profile | Raml08Profile | OasProfile | Oas20Profile | Oas30Profile | AmfProfile =>
        baseOptions.withPartialValidation()
      case _ =>
        baseOptions.withFullValidation()
    }
    ExecutionLog.log("WebApiValidations#validationRequestsForBaseUnit: validating now WebAPI")
    RuntimeValidator
      .shaclValidation(validationContext.baseUnit,
                       validationContext.validations,
                       CustomShaclFunctions.functions,
                       options)
      .map { report =>
        report.results.flatMap { buildValidationResult }
      }
  }

  override def endStep: Boolean = true

  private def buildValidationResult(r: ValidationResult): Option[AMFValidationResult] = {
    buildValidationResult(validationContext.baseUnit, r, validationContext.messageStyle, validationContext.validations)
  }
}

case class ExamplesValidationStep(override val validationContext: ValidationContext) extends ValidationStep {

  override protected def validate(): Future[Seq[AMFValidationResult]] = {
    UnitPayloadsValidation(validationContext.baseUnit, validationContext.platform)
      .validate(validationContext.env)
      .map { results =>
        results.flatMap { buildValidationWithCustomLevelForProfile }
      }
  }

  override def endStep: Boolean = true

  private def buildValidationWithCustomLevelForProfile(result: AMFValidationResult): Option[AMFValidationResult] = {
    Some(result.copy(level = findLevel(result.validationId, validationContext.validations)))
  }
}

case class ParserValidationStep(override val validationContext: ValidationContext) extends ValidationStep {
  override protected def validate(): Future[Seq[AMFValidationResult]] =
    RuntimeValidator
      .aggregateReport(validationContext.baseUnit, validationContext.profile, validationContext.messageStyle)
      .map(_.results)

  override def endStep: Boolean = true
}

case class ValidationContext(baseUnit: BaseUnit,
                             profile: ProfileName,
                             platform: Platform,
                             messageStyle: MessageStyle,
                             validations: EffectiveValidations,
                             env: Environment)
