package amf.facades

import amf.{MessageStyle, ProfileName, RAMLStyle}
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, AMFValidationResult, EffectiveValidations}
import amf.internal.environment.Environment
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.webapi.{Oas20Plugin, PayloadPlugin, Raml08Plugin, Raml10Plugin, _}
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.plugins.features.validation.model.ValidationDialectText
import amf.plugins.syntax.SYamlSyntaxPlugin

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Validation(platform: Platform) {

  def init(): Future[Unit] = {
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    amf.core.AMF.init().map { _ =>
      amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMLPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(JsonSchemaPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(WebAPIDomainPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)

      RuntimeValidator.validatorOption match {
        case Some(AMFValidatorPlugin) =>
          AMFValidatorPlugin.reset()
        case _ =>
      }
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
    AMLPlugin.registry.registerDialect(url, ValidationDialectText.text)
    /*
    platform.dialectsRegistry.get("%Validation Profile 1.0") match {
      case Some(dialect) => Promise().success(dialect).future
      case None          => platform.dialectsRegistry.registerDialect(url, ValidationDialectText.text)
    }
   */
  }

  var profile: Option[ValidationProfile] = None

  // The aggregated report
  def reset(): Unit = validator.reset()

  def aggregatedReport: List[AMFValidationResult] = {
    val set = validator.aggregatedReport.keySet
    if (set.isEmpty) Nil
    else
      validator.aggregatedReport(set.max).toList
  }

  // disable temporarily the reporting of validations
  def enabled: Boolean = validator.enabled

  def withEnabledValidation(enabled: Boolean): Validation = {
    validator.withEnabledValidation(enabled)
    this
  }

  def disableValidations[T]()(f: () => T): T = validator.disableValidations()(f)

  def loadValidationProfile(validationProfilePath: String): Future[ProfileName] = {
    validator.loadValidationProfile(validationProfilePath)
  }

  /**
    * Loads a validation profile generated out of a RAML Dialect
    * @param dialect RAML dialect to be parsed as a Validation Profile
    */
  def loadDialectValidationProfile(dialect: Dialect): Unit =
    // TODO: REDO THIS!!!
    profile = None // Some(new AMFDialectValidations(dialect).profile())

  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle = RAMLStyle,
               env: Environment = Environment()): Future[AMFValidationReport] = {

    validator.validate(model, profileName, messageStyle, env)
  }

  def computeValidations(profileName: ProfileName): EffectiveValidations = validator.computeValidations(profileName)

  def shapesGraph(validations: EffectiveValidations, messageStyle: MessageStyle = RAMLStyle): String =
    validator.shapesGraph(validations, messageStyle)
}

object Validation {
  def apply(platform: Platform): Future[Validation] = {
    val validation = new Validation(platform)
    validation.init().map(_ => validation)
  }
}
