package amf.client

import amf.client.convert.ClientPayloadPluginConverter
import amf.client.convert.ClientPayloadPluginConverter.AMFPluginConverter
import amf.client.convert.CoreClientConverters._
import amf.client.environment.Environment
import amf.client.execution.BaseExecutionEnvironment
import amf.client.model.document.{BaseUnit, Dialect}
import amf.client.plugins.{ClientAMFPayloadValidationPlugin, ClientAMFPlugin}
import amf.client.render._
import amf.client.resolve._
import amf.client.validate.AMFValidationReport
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.{Vocabularies, WebApi}
import amf.plugins.features.AMFCustomValidation
import amf.plugins.{document, features}
import amf.{Core, MessageStyle, ProfileName}

import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}

@JSExportTopLevel("AMF")
object AMF extends PlatformSecrets {

  @JSExport
  def init(): ClientFuture[Unit] = init(platform.defaultExecutionEnvironment)

  def init(executionEnvironment: BaseExecutionEnvironment): ClientFuture[Unit] = {
    WebApi.register(executionEnvironment)
    Vocabularies.register()
    AMFCustomValidation.register()
    amf.Core.registerPlugin(PayloadValidatorPlugin)
    amf.Core.init(executionEnvironment)
  }

  @JSExport
  def validate(model: BaseUnit, profileName: ProfileName): ClientFuture[AMFValidationReport] =
    Core.validate(model, profileName)

  @JSExport
  def validate(model: BaseUnit, profileName: ProfileName, env: Environment): ClientFuture[AMFValidationReport] =
    Core.validate(model, profileName, env)

  /**
    * This method receives a resolved model. Don't use it with an unresolved one.
    */
  @JSExport
  def validateResolved(model: BaseUnit, profileName: ProfileName): ClientFuture[AMFValidationReport] =
    Core.validateResolved(model, profileName)

  /**
    * This method receives a resolved model. Don't use it with an unresolved one.
    */
  @JSExport
  def validateResolved(model: BaseUnit,
                       profileName: ProfileName,
                       env: Environment): ClientFuture[AMFValidationReport] =
    Core.validateResolved(model, profileName, env)

  @JSExport def loadValidationProfile(url: String): ClientFuture[ProfileName] = Core.loadValidationProfile(url)

  @JSExport def loadValidationProfile(url: String, env: Environment): ClientFuture[ProfileName] =
    Core.loadValidationProfile(url, env)

  @JSExport def emitShapesGraph(profileName: ProfileName): String =
    Core.emitShapesGraph(profileName)

  @JSExport def registerDialect(url: String): ClientFuture[Dialect] = Vocabularies.registerDialect(url)

  @JSExport
  def registerDialect(url: String, env: Environment): ClientFuture[Dialect] = Vocabularies.registerDialect(url, env)

  @JSExport def resolveRaml10(unit: BaseUnit): BaseUnit = new Raml10Resolver().resolve(unit)

  @JSExport def resolveRaml08(unit: BaseUnit): BaseUnit = new Raml08Resolver().resolve(unit)

  @JSExport def resolveOas20(unit: BaseUnit): BaseUnit = new Oas20Resolver().resolve(unit)

  @JSExport def resolveAmfGraph(unit: BaseUnit): BaseUnit = new AmfGraphResolver().resolve(unit)
}

@JSExportAll
@JSExportTopLevel("Core")
object CoreWrapper {
  def init(): ClientFuture[Unit] = Core.init()

  def resolver(vendor: String): Resolver = Core.resolver(vendor)

  def validate(model: BaseUnit, profileName: ProfileName): ClientFuture[AMFValidationReport] =
    Core.validate(model, profileName)

  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle,
               env: Environment): ClientFuture[AMFValidationReport] =
    Core.validate(model, profileName, env)

  def loadValidationProfile(url: String): ClientFuture[ProfileName] = Core.loadValidationProfile(url)

  def loadValidationProfile(url: String, env: Environment): ClientFuture[ProfileName] =
    Core.loadValidationProfile(url, env)

  def emitShapesGraph(profileName: ProfileName): String =
    Core.emitShapesGraph(profileName)

  def registerPlugin(plugin: ClientAMFPlugin): Unit = Core.registerPlugin(AMFPluginConverter.asInternal(plugin))

  def registerPayloadPlugin(plugin: ClientAMFPayloadValidationPlugin): Unit =
    Core.registerPlugin(ClientPayloadPluginConverter.convert(plugin))

}

object PluginsWrapper {
  val document: DocumentPluginsWrapper.type = DocumentPluginsWrapper
  val features: FeaturesPluginsWrapper.type = FeaturesPluginsWrapper
}

object DocumentPluginsWrapper {
  val WebApi: document.WebApi.type             = document.WebApi
  val Vocabularies: document.Vocabularies.type = document.Vocabularies
}

object FeaturesPluginsWrapper {
  val AMFValidation: features.AMFValidation.type             = features.AMFValidation
  val AMFCustomValidation: features.AMFCustomValidation.type = features.AMFCustomValidation
}
