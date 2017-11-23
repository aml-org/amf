package amf.framework.plugins

import amf.framework.document.BaseUnit
import amf.framework.validation.{AMFValidationReport, EffectiveValidations}
import amf.remote.Platform
import amf.validation.model.ValidationProfile

import scala.concurrent.Future

trait AMFValidationPlugin extends AMFPlugin {
  /**
    * Validation profiles supported by this plugin by default
    */
  def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile]

  /**
    * Request for validation of a particular model, profile and list of effective validations form that profile
    */
  def validationRequest(baseUnit: BaseUnit, profile: String, validations: EffectiveValidations, platform: Platform): Future[AMFValidationReport]
}
