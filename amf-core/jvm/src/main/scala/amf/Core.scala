package amf

import java.util.concurrent.CompletableFuture

import amf.client.convert.CoreRegister
import amf.client.model.document._
import amf.client.render.Renderer
import amf.core.AMF
import amf.core.client.{Parser, Resolver, Validator}
import amf.core.plugins.AMFPlugin
import amf.core.remote.FutureConverter._
import amf.core.unsafe.PlatformSecrets
import amf.validation.AMFValidationReport

object Core extends PlatformSecrets {

  def init(): CompletableFuture[Nothing] = {
    CoreRegister.register(platform)

    AMF.init().asJava
  }

  def parser(vendor: String, mediaType: String): Parser      = new Parser(vendor, mediaType)
  def generator(vendor: String, mediaType: String): Renderer = new Renderer(vendor, mediaType)
  def resolver(vendor: String)                               = new Resolver(vendor)
  def validate(model: BaseUnit,
               profileName: String,
               messageStyle: String = "AMF"): CompletableFuture[AMFValidationReport] =
    Validator.validate(model, profileName, messageStyle)
  def loadValidationProfile(url: String): CompletableFuture[Nothing] = Validator.loadValidationProfile(url)
  def registerNamespace(alias: String, prefix: String): Boolean      = platform.registerNamespace(alias, prefix).isDefined
  def registerPlugin(plugin: AMFPlugin): Unit                        = AMF.registerPlugin(plugin)
}
