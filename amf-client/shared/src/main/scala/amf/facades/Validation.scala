package amf.facades

import amf.ProfileNames
import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, AMFValidationResult, EffectiveValidations}
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.vocabularies2.{ RAMLVocabulariesPlugin => RAMLVocabularies2Plugin }
import amf.plugins.document.vocabularies.registries.PlatformDialectRegistry
import amf.plugins.document.vocabularies.spec.Dialect
import amf.plugins.document.vocabularies.validation.AMFDialectValidations
import amf.plugins.document.webapi._
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
    amf.core.AMF.init().map { _ =>
      amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(OAS30Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(RAMLVocabulariesPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(JsonSchemaPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(RAMLVocabularies2Plugin)
      amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(WebAPIDomainPlugin)
      amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)

      RuntimeValidator.validator match {
        case Some(AMFValidatorPlugin) =>
          AMFValidatorPlugin.reset()
        case _ =>
      }
    }
  }

  lazy val validator: AMFValidatorPlugin.type = RuntimeValidator.validator.get.asInstanceOf[AMFValidatorPlugin.type]
  //

  val url = "http://raml.org/dialects/profile.raml"

  /**
    * Loads the validation dialect from the provided URL
    */
  def loadValidationDialect(): Future[Dialect] = {
    PlatformDialectRegistry.registerDialect(url, ValidationDialectText.text)
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

  def aggregatedReport: List[AMFValidationResult] = validator.aggregatedReport

  // disable temporarily the reporting of validations
  def enabled: Boolean = validator.enabled

  def withEnabledValidation(enabled: Boolean): Validation = {
    validator.withEnabledValidation(enabled)
    this
  }

  def disableValidations[T]()(f: () => T): T = validator.disableValidations()(f)

  /**
    * Client code can use this function to register a new validation failure
    */
  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None): Unit = {
    validator.reportConstraintFailure(level, validationId, targetNode, targetProperty, message, position)
  }

  def loadValidationProfile(validationProfilePath: String): Future[String] = {
    validator.loadValidationProfile(validationProfilePath)
  }

  /**
    * Loads a validation profile generated out of a RAML Dialect
    * @param dialect RAML dialect to be parsed as a Validation Profile
    */
  def loadDialectValidationProfile(dialect: Dialect): Unit =
    profile = Some(new AMFDialectValidations(dialect).profile())

  def validate(model: BaseUnit,
               profileName: String,
               messageStyle: String = ProfileNames.RAML): Future[AMFValidationReport] = {

    validator.validate(model, profileName, messageStyle)
  }

  def computeValidations(profileName: String): EffectiveValidations = validator.computeValidations(profileName)

  def shapesGraph(validations: EffectiveValidations, messageStyle: String = ProfileNames.RAML): String =
    validator.shapesGraph(validations, messageStyle)
}

object Validation {
  def apply(platform: Platform): Future[Validation] = {
    val validation = new Validation(platform)
    validation.init().map(_ => validation)
  }
}
