package amf.facades

import amf.ProfileNames
import amf.core.AMF
import amf.core.model.document.BaseUnit
import amf.core.model.domain.LexicalInformation
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.core.validation.core.ValidationDialectText
import amf.core.validation.{AMFValidationReport, AMFValidationResult, EffectiveValidations}
import amf.plugins.document.vocabularies.spec.Dialect
import amf.plugins.document.vocabularies.validation.AMFDialectValidations
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.plugins.features.validation.model._

import scala.concurrent.Future


class Validation(platform: Platform) {

  // Temporary
  RuntimeValidator.validator match {
    case None =>
      AMF.init()
      AMFValidatorPlugin.init(platform)
    case Some(runtimeValidator) =>
      runtimeValidator.asInstanceOf[AMFValidatorPlugin].enabled = true
      runtimeValidator.reset()
  }
  val validator = RuntimeValidator.validator.get.asInstanceOf[AMFValidatorPlugin]
  //

  val url = "http://raml.org/dialects/profile.raml"

  /**
    * Loads the validation dialect from the provided URL
    */
  def loadValidationDialect(): Future[Dialect] = {
    platform.dialectsRegistry.registerDialect(url, ValidationDialectText.text)
    /*
    platform.dialectsRegistry.get("%Validation Profile 1.0") match {
      case Some(dialect) => Promise().success(dialect).future
      case None          => platform.dialectsRegistry.registerDialect(url, ValidationDialectText.text)
    }
    */
  }

  var profile: Option[ValidationProfile] = None

  // The aggregated report
  def reset(): Unit = validator.reset()

  def aggregatedReport: List[AMFValidationResult] = validator.aggregatedReport

  // disable temporarily the reporting of validations
  def enabled: Boolean = validator.enabled

  def withEnabledValidation(enabled: Boolean): Validation = {
    validator.withEnabledValidation(enabled)
    this
  }

  def disableValidations[T]()(f: () => T): T = validator.disableValidations()(f)

  /**
    * Client code can use this function to register a new validation failure
    */
  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None): Unit = {
    validator.reportConstraintFailure(level, validationId, targetNode, targetProperty, message, position)
  }

  def loadValidationProfile(validationProfilePath: String): Future[Unit] = {
    validator.loadValidationProfile(validationProfilePath)
  }

  /**
    * Loads a validation profile generated out of a RAML Dialect
    * @param dialect RAML dialect to be parsed as a Validation Profile
    */
  def loadDialectValidationProfile(dialect: Dialect): Unit =
    profile = Some(new AMFDialectValidations(dialect).profile())


  def validate(model: BaseUnit,
               profileName: String,
               messageStyle: String = ProfileNames.RAML): Future[AMFValidationReport] = {

    validator.validate(model, profileName, messageStyle)
  }

  def computeValidations(profileName: String): EffectiveValidations = validator.computeValidations(profileName)

  def shapesGraph(validations: EffectiveValidations, messageStyle: String = ProfileNames.RAML): String = validator.shapesGraph(validations, messageStyle)
}

object ValidationMutex {}
object Validation {
  def apply(platform: Platform) = new Validation(platform)
}
