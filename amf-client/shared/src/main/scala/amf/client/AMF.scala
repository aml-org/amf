package amf.client

import amf.client.convert.ClientPayloadPluginConverter
import amf.client.convert.CoreClientConverters._
import amf.client.environment.Environment
import amf.client.execution.BaseExecutionEnvironment
import amf.client.model.document.{BaseUnit, Dialect}
import amf.client.parse._
import amf.client.plugins.{AMFPlugin, ClientAMFPayloadValidationPlugin}
import amf.client.render._
import amf.client.resolve._
import amf.client.validate.ValidationReport
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.{Vocabularies, WebApi}
import amf.plugins.features.AMFValidation
import amf.plugins.{document, features}
import amf.{AMFStyle, Core, MessageStyle, ProfileName}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("AMF")
object AMF extends PlatformSecrets {

  def init(): ClientFuture[Unit] = init(platform.defaultExecutionEnvironment)

  def init(executionEnvironment: BaseExecutionEnvironment): ClientFuture[Unit] = {
    WebApi.register(executionEnvironment)
    Vocabularies.register()
    AMFValidation.register()
    amf.Core.registerPlugin(PayloadValidatorPlugin)
    amf.Core.init(executionEnvironment)
  }

  def raml10Parser(): Raml10Parser = new Raml10Parser()

  def ramlParser(): RamlParser = new RamlParser()

  def raml10Generator(): Raml10Renderer = new Raml10Renderer()

  def raml08Parser(): Raml08Parser = new Raml08Parser()

  def raml08Generator(): Raml08Renderer = new Raml08Renderer()

  def oas20Parser(): Oas20Parser = new Oas20Parser()

  def oas20Generator(): Oas20Renderer = new Oas20Renderer()

  def amfGraphParser(): AmfGraphParser = new AmfGraphParser()

  def amfGraphGenerator(): AmfGraphRenderer = new AmfGraphRenderer()

  def validate(model: BaseUnit, profileName: ProfileName, messageStyle: MessageStyle): ClientFuture[ValidationReport] =
    Core.validate(model, profileName, messageStyle)

  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle,
               env: Environment): ClientFuture[ValidationReport] =
    Core.validate(model, profileName, messageStyle, env)

  /**
    * This method receives a resolved model. Don't use it with an unresolved one.
    */
  def validateResolved(model: BaseUnit,
                       profileName: ProfileName,
                       messageStyle: MessageStyle): ClientFuture[ValidationReport] =
    Core.validateResolved(model, profileName, messageStyle)

  /**
    * This method receives a resolved model. Don't use it with an unresolved one.
    */
  def validateResolved(model: BaseUnit,
                       profileName: ProfileName,
                       messageStyle: MessageStyle,
                       env: Environment): ClientFuture[ValidationReport] =
    Core.validateResolved(model, profileName, messageStyle, env)

  def loadValidationProfile(url: String): ClientFuture[ProfileName] = Core.loadValidationProfile(url)

  def loadValidationProfile(url: String, env: Environment): ClientFuture[ProfileName] =
    Core.loadValidationProfile(url, env)

  def emitShapesGraph(profileName: ProfileName): String =
    Core.emitShapesGraph(profileName)

  def registerNamespace(alias: String, prefix: String): Boolean = Core.registerNamespace(alias, prefix)

  def registerDialect(
      url: String,
      executionEnvironment: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): ClientFuture[Dialect] =
    Vocabularies.registerDialect(url, executionEnvironment)

  def resolveRaml10(unit: BaseUnit): BaseUnit = new Raml10Resolver().resolve(unit)

  def resolveRaml08(unit: BaseUnit): BaseUnit = new Raml08Resolver().resolve(unit)

  def resolveOas20(unit: BaseUnit): BaseUnit = new Oas20Resolver().resolve(unit)

  def resolveAmfGraph(unit: BaseUnit): BaseUnit = new AmfGraphResolver().resolve(unit)

  def jsonPayloadParser(): JsonPayloadParser = new JsonPayloadParser()

  def yamlPayloadParser(): YamlPayloadParser = new YamlPayloadParser()
}

@JSExportAll
@JSExportTopLevel("Core")
object CoreWrapper {
  def init(): ClientFuture[Unit] = Core.init()

  def parser(vendor: String, mediaType: String): Parser = Core.parser(vendor, mediaType)

  def generator(vendor: String, mediaType: String): Renderer = Core.generator(vendor, mediaType)

  def resolver(vendor: String): Resolver = Core.resolver(vendor)

  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle = AMFStyle): ClientFuture[ValidationReport] =
    Core.validate(model, profileName, messageStyle)

  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle,
               env: Environment): ClientFuture[ValidationReport] =
    Core.validate(model, profileName, messageStyle, env)

  def loadValidationProfile(url: String): ClientFuture[ProfileName] = Core.loadValidationProfile(url)

  def loadValidationProfile(url: String, env: Environment): ClientFuture[ProfileName] =
    Core.loadValidationProfile(url, env)

  def emitShapesGraph(profileName: ProfileName): String =
    Core.emitShapesGraph(profileName)

  def registerNamespace(alias: String, prefix: String): Boolean = Core.registerNamespace(alias, prefix)

  def registerPlugin(plugin: AMFPlugin): Unit = Core.registerPlugin(plugin)

  def registerPayloadPlugin(plugin: ClientAMFPayloadValidationPlugin): Unit =
    Core.registerPlugin(ClientPayloadPluginConverter.convert(plugin))

}

@JSExportAll
@JSExportTopLevel("plugins")
object PluginsWrapper {
  val document: DocumentPluginsWrapper.type = DocumentPluginsWrapper
  val features: FeaturesPluginsWrapper.type = FeaturesPluginsWrapper
}

@JSExportAll
object DocumentPluginsWrapper {
  val WebApi: document.WebApi.type             = document.WebApi
  val Vocabularies: document.Vocabularies.type = document.Vocabularies
}

@JSExportAll
object FeaturesPluginsWrapper {
  val AMFValidation: features.AMFValidation.type = features.AMFValidation
}
