package amf.client.validate

import amf.client.convert.CoreClientConverters._
import amf.client.model.document.BaseUnit
import amf.core.services.RuntimeValidator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object Validator {

  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF"): ClientFuture[ValidationReport] =
    RuntimeValidator(
      model._internal,
      profileName,
      messageStyle
    ).map(report => report).asClient

  def loadValidationProfile(url: String): ClientFuture[String] = RuntimeValidator.loadValidationProfile(url).asClient
}
