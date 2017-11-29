package amf.core.client

import java.io.File

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

  def loadValidationProfile(path: File) = {
    RuntimeValidator.loadValidationProfile("file://" + path.getAbsolutePath).asJava
  }
}
