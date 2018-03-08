package amf

import amf.client.convert.CoreRegister
import amf.core.AMF
import amf.core.client.{Generator, Parser, Resolver, Validator}
import amf.core.plugins.AMFPlugin
import amf.core.unsafe.PlatformSecrets
import amf.client.model.document._
import amf.validation.AMFValidationReport
import amf.client.model.domain._

import scala.scalajs.js.JSConverters._
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSExportAll
import scala.concurrent.ExecutionContext.Implicits.global

@JSExportAll
object Core extends PlatformSecrets {

  def init(): Promise[Unit] = {

    CoreRegister.register(platform)

    // Init the core component
    AMF.init().toJSPromise
  }

  def parser(vendor: String, mediaType: String): Parser       = new Parser(vendor, mediaType)
  def generator(vendor: String, mediaType: String): Generator = new Generator(vendor, mediaType)
  def resolver(vendor: String): Resolver                      = new Resolver(vendor)
  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF"): Promise[AMFValidationReport] =
    Validator.validate(model, profileName, messageStyle)
  def loadValidationProfile(url: String): Promise[String]       = Validator.loadValidationProfile(url)
  def registerNamespace(alias: String, prefix: String): Boolean = platform.registerNamespace(alias, prefix).isDefined
  def registerPlugin(plugin: AMFPlugin): Unit                   = AMF.registerPlugin(plugin)
}
