package amf.plugins.document.webapi.validation

import amf.ProfileNames.{MessageStyle, ProfileName}
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.validation._
import amf.core.validation.core.{ValidationProfile, ValidationResult, ValidationSpecification}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline

import scala.concurrent.Future

trait WebApiValidations extends ValidationResultProcessor {

  var aggregatedReport: List[AMFValidationResult] = List()

  val defaultValidationProfiles = DefaultAMFValidations.profiles().foldLeft(Map[String, () => ValidationProfile]()) {
    case (acc, profile) =>
      acc.updated(profile.name.profile, { () =>
        profile
      })
  }

  protected def validationRequestsForBaseUnit(unresolvedUnit: BaseUnit,
                                              profile: ProfileName,
                                              validations: EffectiveValidations,
                                              messageStyle: MessageStyle,
                                              platform: Platform): Future[AMFValidationReport] = {

    // Before validating we need to resolve to get all the model information
    val baseUnit = new ValidationResolutionPipeline(profile, unresolvedUnit).resolve()
    WebApiValidationsRunner(ValidationContext(baseUnit, profile, platform, messageStyle, validations)).runSteps
  }

  protected def buildPayloadValidationResult(model: BaseUnit,
                                             result: ValidationResult,
                                             validations: EffectiveValidations): Option[AMFValidationResult] = {
    val validationSpecToLook = if (result.sourceShape.startsWith(Namespace.Data.base)) {
      result.sourceShape
        .replace(Namespace.Data.base, "") // this is for custom validations they are all prefixed with the data namespace
    } else {
      result.sourceShape // by default we expect to find a URI here
    }
    val maybeTargetSpec: Option[ValidationSpecification] = validations.all.get(validationSpecToLook) match {
      case Some(validationSpec) =>
        Some(validationSpec)

      case None =>
        validations.all.find {
          case (v, validation) =>
            // processing property shapes Id computed as constraintID + "/prop"
            validation.propertyConstraints.find(p => p.name == validationSpecToLook) match {
              case Some(p) => true
              case None    => validationSpecToLook.startsWith(v)
            }
        } match {
          case Some((v, spec)) =>
            Some(spec)
          case None =>
            if (validationSpecToLook.startsWith("_:")) {
              None
            } else {
              throw new Exception(s"Cannot find validation spec for validation error:\n $result")
            }
        }
    }

    maybeTargetSpec match {
      case Some(targetSpec) =>
        val propertyConstraint = targetSpec.propertyConstraints.find(p => p.name == validationSpecToLook)

        var message = propertyConstraint match {
          case Some(p) => p.message.getOrElse(targetSpec.message)
          case None    => targetSpec.message
        }

        if (Option(message).isEmpty || message == "") {
          message = result.message.getOrElse("Constraint violation")
        }

        val finalId = propertyConstraint match {
          case Some(p) => p.name
          case None    => targetSpec.name
        }
        val severity = SeverityLevels.VIOLATION
        Some(
          AMFValidationResult.withShapeId(finalId,
                                          AMFValidationResult.fromSHACLValidation(model, message, severity, result)))
      case _ => None
    }
  }

}
