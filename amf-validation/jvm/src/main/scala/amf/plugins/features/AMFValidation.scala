package amf.plugins.features

import amf.plugins.features.validation.AMFValidatorPlugin

object AMFValidation {
  def register() = {
    amf.Core.registerPlugin(AMFValidatorPlugin)
  }
}
