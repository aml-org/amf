package amf.core.validation

import amf.core.model.document.BaseUnit
import amf.core.model.domain.LexicalInformation
import amf.core.plugins.AMFPlugin
import amf.core.services.RuntimeValidator
import amf.core.validation.core.{ValidationReport, ValidationResult}
import amf.plugins.features.validation.model.DefaultAMFValidations

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ParserSideValidationPlugin extends AMFPlugin with RuntimeValidator with ValidationResultProcessor {
  override val ID: String = "Parser side AMF Validation"

  override def dependencies(): Seq[AMFPlugin] = Seq()

  var aggregatedReport: List[AMFValidationResult] = List()

  // The aggregated report
  def reset(): Unit = {
    enabled = true
    aggregatedReport = List()
  }

  // disable temporarily the reporting of validations
  var enabled: Boolean = true

  def withEnabledValidation(enabled: Boolean): RuntimeValidator = {
    this.enabled = enabled
    this
  }

  def disableValidations[T]()(f: () => T): T = {
    if (enabled) {
      enabled = false
      try {
        f()
      } finally {
        enabled = true
      }
    } else {
      f()
    }
  }


  def disableValidationsAsync[T]()(f: (() => Unit) => T): T = {
    if (enabled) {
      enabled = false
      try {
        f(() => enabled = true)
      } catch {
        case e: Exception =>
          enabled = true
          throw e
      }
    } else {
      f(() => {})
    }
  }



  /**
    * Loads a validation profile from a URL
    */
  override def loadValidationProfile(validationProfilePath: String): Future[Unit] = Future {}

  /**
    * Low level validation returning a SHACL validation report
    */
  override def shaclValidation(model: BaseUnit, validations: EffectiveValidations, messageStyle: String): Future[ValidationReport] = Future {
    new ValidationReport {
      override def conforms: Boolean = false
      override def results: List[ValidationResult] = Nil
    }
  }

  /**
    * Main validation function returning an AMF validation report linking validation errors
    * for validations in the profile to domain elements in the model
    */
  override def validate(model: BaseUnit, profileName: String, messageStyle: String): Future[AMFValidationReport] = {
    val validations = EffectiveValidations().someEffective(DefaultAMFValidations.parserSideValidationsProfile)
    // aggregating parser-side validations
    var results = aggregatedReport.map(r => processAggregatedResult(r, messageStyle, validations))

    Future {
      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = model.id,
        profile = profileName,
        results = results
      )
    }
  }

  /**
    * Client code can use this function to register a new validation failure
    */
  override def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None): Unit = {
    val validationError = AMFValidationResult(message, level, targetNode, targetProperty, validationId, position)
    if (enabled) {
      aggregatedReport ++= Seq(validationError)
    } else {
      throw new Exception(validationError.toString)
    }
  }

}

object ParserSideValidationPlugin {
  def apply() = new ParserSideValidationPlugin
}
