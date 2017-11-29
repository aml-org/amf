package amf.core.client

import amf.core.remote.FutureConverter._
import amf.core.services.RuntimeValidator
import amf.core.validation.AMFValidationReport
import amf.model.document.BaseUnit

object Validator {

  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF") = RuntimeValidator(
    model.element,
    profileName,
    messageStyle
  ).asJava[AMFValidationReport]

  def loadValidationProfile(url: String) = {
    RuntimeValidator.loadValidationProfile(url).asJava
  }
}
