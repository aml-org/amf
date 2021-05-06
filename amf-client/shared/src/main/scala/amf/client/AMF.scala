package amf.client

import amf.client.convert.ClientPayloadPluginConverter
import amf.client.convert.ClientPayloadPluginConverter.AMFPluginConverter
import amf.client.convert.CoreClientConverters._
import amf.client.environment.Environment
import amf.client.execution.BaseExecutionEnvironment
import amf.client.model.document.{BaseUnit, Dialect}
import amf.client.parse._
import amf.client.plugins.{AMFPlugin, ClientAMFPayloadValidationPlugin, ClientAMFPlugin}
import amf.client.render._
import amf.client.resolve._
import amf.client.validate.ValidationReport
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.{Vocabularies, WebApi}
import amf.plugins.features.AMFCustomValidation
import amf.plugins.{document, features}
import amf.{AMFStyle, Core, MessageStyle, ProfileName}

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

  @JSExport def raml10Parser(): Raml10Parser = new Raml10Parser()

  @JSExport def ramlParser(): RamlParser = new RamlParser()

  @JSExport def raml10Generator(): Raml10Renderer = new Raml10Renderer()

  @JSExport def raml08Parser(): Raml08Parser = new Raml08Parser()

  @JSExport def raml08Generator(): Raml08Renderer = new Raml08Renderer()

  @JSExport def oas20Parser(): Oas20Parser = new Oas20Parser()

  @JSExport def oas20Generator(): Oas20Renderer = new Oas20Renderer()

  @JSExport def amfGraphParser(): AmfGraphParser = new AmfGraphParser()

  @JSExport def amfGraphGenerator(): AmfGraphRenderer = new AmfGraphRenderer()

  @JSExport
  def validate(model: BaseUnit, profileName: ProfileName, messageStyle: MessageStyle): ClientFuture[ValidationReport] =
    Core.validate(model, profileName, messageStyle)

  @JSExport
  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle,
               env: Environment): ClientFuture[ValidationReport] =
    Core.validate(model, profileName, messageStyle, env)

  /**
    * This method receives a resolved model. Don't use it with an unresolved one.
    */
  @JSExport
  def validateResolved(model: BaseUnit,
                       profileName: ProfileName,
                       messageStyle: MessageStyle): ClientFuture[ValidationReport] =
    Core.validateResolved(model, profileName, messageStyle)

  /**
    * This method receives a resolved model. Don't use it with an unresolved one.
    */
  @JSExport
  def validateResolved(model: BaseUnit,
                       profileName: ProfileName,
                       messageStyle: MessageStyle,
                       env: Environment): ClientFuture[ValidationReport] =
    Core.validateResolved(model, profileName, messageStyle, env)

  @JSExport def loadValidationProfile(url: String): ClientFuture[ProfileName] = Core.loadValidationProfile(url)

  @JSExport def loadValidationProfile(url: String, env: Environment): ClientFuture[ProfileName] =
    Core.loadValidationProfile(url, env)

  @JSExport def emitShapesGraph(profileName: ProfileName): String =
    Core.emitShapesGraph(profileName)

  @JSExport def registerNamespace(alias: String, prefix: String): Boolean = Core.registerNamespace(alias, prefix)

  @JSExport def registerDialect(url: String): ClientFuture[Dialect] = Vocabularies.registerDialect(url)

  @JSExport
  def registerDialect(url: String, env: Environment): ClientFuture[Dialect] = Vocabularies.registerDialect(url, env)

  @JSExport def resolveRaml10(unit: BaseUnit): BaseUnit = new Raml10Resolver().resolve(unit)

  @JSExport def resolveRaml08(unit: BaseUnit): BaseUnit = new Raml08Resolver().resolve(unit)

  @JSExport def resolveOas20(unit: BaseUnit): BaseUnit = new Oas20Resolver().resolve(unit)

  @JSExport def resolveAmfGraph(unit: BaseUnit): BaseUnit = new AmfGraphResolver().resolve(unit)

  @JSExport def jsonPayloadParser(): JsonPayloadParser = new JsonPayloadParser()

  @JSExport def yamlPayloadParser(): YamlPayloadParser = new YamlPayloadParser()
}

@JSExportAll
@JSExportTopLevel("Core")
object CoreWrapper {
  def init(): ClientFuture[Unit] = Core.init()

  def parser(vendor: String, mediaType: String): Parser = Core.parser(vendor, mediaType)

  def parser(vendor: String, mediaType: String, env: Environment): Parser = Core.parser(vendor, mediaType, env)

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

  def registerPlugin(plugin: ClientAMFPlugin): Unit = Core.registerPlugin(AMFPluginConverter.asInternal(plugin))

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
  val AMFValidation: features.AMFValidation.type             = features.AMFValidation
  val AMFCustomValidation: features.AMFCustomValidation.type = features.AMFCustomValidation
}
