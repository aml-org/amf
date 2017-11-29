package amf.core.services

import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.validation.core.ValidationReport
import amf.core.validation.{AMFValidationReport, EffectiveValidations}

import scala.concurrent.Future

/**
  * Validation of AMF models
  */
trait RuntimeValidator {

  /**
    * Loads a validation profile from a URL
    */
  def loadValidationProfile(validationProfilePath: String): Future[String]

  /**
    * Low level validation returning a SHACL validation report
    */
  def shaclValidation(model: BaseUnit, validations: EffectiveValidations, messageStyle: String): Future[ValidationReport]

  /**
    * Main validation function returning an AMF validation report linking validation errors
    * for validations in the profile to domain elements in the model
    */
  def validate(model: BaseUnit, profileName: String, messageStyle: String): Future[AMFValidationReport]

  def reset()

  /**
    * Client code can use this function to register a new validation failure
    */
  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None)

  /**
    * Temporary disable checking of runtime validations for the duration of the passed block
    */
  def disableValidations[T]()(f: () => T): T

  /**
    * Async version of disable valdiations
    */
  def disableValidationsAsync[T]()(f: (() => Unit) => T): T
}

object RuntimeValidator {
  var validator: Option[RuntimeValidator] = None
  def register(runtimeValidator: RuntimeValidator) = {
    validator = Some(runtimeValidator)
  }

  def loadValidationProfile(validationProfilePath: String) = {
    validator match {
      case Some(runtimeValidator) => runtimeValidator.loadValidationProfile(validationProfilePath)
      case None                   => throw new Exception("No registered runtime validator")
    }
  }

  def shaclValidation(model: BaseUnit, validations: EffectiveValidations, messgeStyle: String = "AMF"): Future[ValidationReport] = {
    validator match {
      case Some(runtimeValidator) => runtimeValidator.shaclValidation(model, validations, messgeStyle)
      case None                   => throw new Exception("No registered runtime validator")
    }
  }

  def apply(model: BaseUnit, profileName: String, messageStyle: String = "AMF"): Future[AMFValidationReport] = {
    validator match {
      case Some(runtimeValidator) => runtimeValidator.validate(model, profileName, messageStyle)
      case None                   => throw new Exception("No registered runtime validator")
    }
  }

  def reset() = {
    validator match {
      case Some(runtimeValidator) => runtimeValidator.reset()
      case None                   => throw new Exception("No registered runtime validator")
    }
  }

  def disableValidations[T]()(f: () => T): T = validator match {
    case Some(runtimeValidator) => runtimeValidator.disableValidations()(f)
    case None                   => throw new Exception("No registered runtime validator")
  }
  def disableValidationsAsync[T]()(f: (() => Unit) => T): T = validator match {
    case Some(runtimeValidator) => runtimeValidator.disableValidationsAsync()(f)
    case None                   => throw new Exception("No registered runtime validator")
  }

  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None) = {
    validator match {
      case Some(runtimeValidator) => runtimeValidator.reportConstraintFailure(
        level,
        validationId,
        targetNode,
        targetProperty,
        message,
        position
      )
      case None => throw new Exception("No registered runtime validator")
    }
  }
}
