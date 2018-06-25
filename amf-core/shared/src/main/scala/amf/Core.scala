package amf

import amf.ProfileNames.{MessageStyle, ProfileName}
import amf.client.convert.CoreClientConverters._
import amf.client.convert.CoreRegister
import amf.client.model.document._
import amf.client.parse.Parser
import amf.client.render.Renderer
import amf.client.resolve.Resolver
import amf.client.validate.{ValidationReport, Validator}
import amf.core.AMF
import amf.core.plugins.AMFPlugin
import amf.core.unsafe.PlatformSecrets

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object Core extends PlatformSecrets {

  def init(): ClientFuture[Unit] = {

    CoreRegister.register(platform)

    // Init the core component
    AMF.init().asClient
  }

  def parser(vendor: String, mediaType: String): Parser = new Parser(vendor, mediaType, None)

  def generator(vendor: String, mediaType: String): Renderer = new Renderer(vendor, mediaType)

  def resolver(vendor: String): Resolver = new Resolver(vendor)

  def validate(model: BaseUnit, profileName: ProfileName, messageStyle: MessageStyle): ClientFuture[ValidationReport] =
    Validator.validate(model, profileName, messageStyle)

  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF"): ClientFuture[ValidationReport] =
    Validator.validate(model, profileName, messageStyle)

  def loadValidationProfile(url: String): ClientFuture[ProfileName] = Validator.loadValidationProfile(url)

  def registerNamespace(alias: String, prefix: String): Boolean = platform.registerNamespace(alias, prefix).isDefined

  def registerPlugin(plugin: AMFPlugin): Unit = AMF.registerPlugin(plugin)
}
