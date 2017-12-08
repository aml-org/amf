package amf.core.client

import amf.core.remote.FutureConverter._
import amf.core.services.RuntimeValidator
import amf.model.document.BaseUnit
import amf.validation.AMFValidationReport

import scala.concurrent.ExecutionContext.Implicits.global

object Validator {

  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF") = RuntimeValidator(
    model.element,
    profileName,
    messageStyle
  ).map(new AMFValidationReport(_))
   .asJava[AMFValidationReport]

  def loadValidationProfile(url: String) = {
    RuntimeValidator.loadValidationProfile(url).asJava
  }
}
