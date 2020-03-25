package amf.plugins.document.webapi.validation.runner

import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.validation.EffectiveValidations
import amf.internal.environment.Environment
import amf.{MessageStyle, ProfileName}

case class ValidationContext(baseUnit: BaseUnit,
                             profile: ProfileName,
                             platform: Platform,
                             messageStyle: MessageStyle,
                             validations: EffectiveValidations,
                             env: Environment)
