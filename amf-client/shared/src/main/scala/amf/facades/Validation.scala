package amf.facades

import amf.client.execution.BaseExecutionEnvironment
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.{AMFValidationReport, EffectiveValidations}
import amf.internal.environment.Environment
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.webapi.{Oas20Plugin, PayloadPlugin, Raml08Plugin, Raml10Plugin, _}
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.plugins.features.validation.custom.model.ValidationDialectText
import amf.plugins.features.validation.custom.AMFValidatorPlugin
import amf.plugins.features.validation.CoreValidations
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.validation.DialectValidations
import amf.validations.{ParserSideValidations, PayloadValidations, RenderSideValidations, ResolutionSideValidations}
import amf.{MessageStyle, ProfileName, RAMLStyle, RamlProfile}

import scala.concurrent.{ExecutionContext, Future}

class Validation(platform: Platform) {

  def init()(implicit executionContext: ExecutionContext): Future[Unit] = {
    platform.registerValidations(CoreValidations.validations, CoreValidations.levels)
    platform.registerValidations(DialectValidations.validations, DialectValidations.levels)
    platform.registerValidations(ParserSideValidations.validations, ParserSideValidations.levels)
    platform.registerValidations(PayloadValidations.validations, PayloadValidations.levels)
    platform.registerValidations(RenderSideValidations.validations, RenderSideValidations.levels)
    platform.registerValidations(ResolutionSideValidations.validations, ResolutionSideValidations.levels)

    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    amf.core.AMF.init().map { _ =>
      amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Async20Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMLPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(JsonSchemaPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(WebAPIDomainPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)
    }
  }

  lazy val validator: AMFValidatorPlugin.type =
    RuntimeValidator.validatorOption.get.asInstanceOf[AMFValidatorPlugin.type]
  //

  val url = "http://a.ml/dialects/profile.raml"

  /**
    * Loads the validation dialect from the provided URL
    */
  def loadValidationDialect(): Future[Dialect] = {
    AMLPlugin().registry.registerDialect(url, ValidationDialectText.text)
    /*
    platform.dialectsRegistry.get("%Validation Profile 1.0") match {
      case Some(dialect) => Promise().success(dialect).future
      case None          => platform.dialectsRegistry.registerDialect(url, ValidationDialectText.text)
    }
   */
  }

  def loadValidationProfile(validationProfilePath: String, errorHandler: ErrorHandler): Future[ProfileName] = {
    validator.loadValidationProfile(validationProfilePath, errorHandler = errorHandler)
  }

  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle = RAMLStyle,
               env: Environment = Environment(),
               resolved: Boolean = false,
               exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): Future[AMFValidationReport] = {

    validator.validate(model, profileName, messageStyle, env, resolved, exec)
  }

  def computeValidations(profileName: ProfileName): EffectiveValidations = validator.computeValidations(profileName)

  def shapesGraph(validations: EffectiveValidations, profileName: ProfileName = RamlProfile): String =
    validator.shapesGraph(validations, profileName)
}

object Validation extends PlatformSecrets {
  def apply(platform: Platform,
            exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): Future[Validation] = {
    implicit val executionContext: ExecutionContext = exec.executionContext
    val validation                                  = new Validation(platform)
    validation.init().map(_ => validation)
  }
}
