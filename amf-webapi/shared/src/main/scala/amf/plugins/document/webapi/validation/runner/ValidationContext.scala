package amf.plugins.document.webapi.validation.runner

import amf.ProfileName
import amf.client.remod.amfcore.plugins.validate.{ValidationConfiguration, ValidationOptions}
import amf.core.model.document.BaseUnit
import amf.core.validation.EffectiveValidations

case class ValidationContext(baseUnit: BaseUnit, private val options: ValidationOptions) {
  val profileName: ProfileName               = options.profileName
  val validations: EffectiveValidations      = options.validations
  val configuration: ValidationConfiguration = options.config
}
