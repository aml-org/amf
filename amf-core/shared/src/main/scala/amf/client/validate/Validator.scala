package amf.client.validate

import amf.{AMFStyle, MessageStyle, ProfileName}
import amf.client.convert.CoreClientConverters._
import amf.client.environment.{DefaultEnvironment, Environment}
import amf.client.model.document.BaseUnit
import amf.core.services.RuntimeValidator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object Validator {

  @deprecated
  def validate(model: BaseUnit, profileName: String, messageStyle: String): ClientFuture[ValidationReport] =
    validate(model, ProfileName(profileName), MessageStyle(messageStyle))

  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle = AMFStyle,
               env: Environment = DefaultEnvironment()): ClientFuture[ValidationReport] =
    RuntimeValidator(
      model._internal,
      profileName,
      messageStyle,
      env._internal
    ).map(report => report).asClient

  def loadValidationProfile(url: String): ClientFuture[ProfileName] =
    RuntimeValidator.loadValidationProfile(url).asClient
}
