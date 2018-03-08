package amf.core.client

import amf.core.services.RuntimeValidator
import amf.client.model.document.BaseUnit
import amf.validation.AMFValidationReport

import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll
import scala.concurrent.ExecutionContext.Implicits.global

@JSExportAll
object Validator {

  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF") =
    RuntimeValidator(
      model._internal,
      profileName,
      messageStyle
    ).map(new AMFValidationReport(_)).toJSPromise

  def loadValidationProfile(url: String) = {
    RuntimeValidator.loadValidationProfile(url).toJSPromise
  }
}
