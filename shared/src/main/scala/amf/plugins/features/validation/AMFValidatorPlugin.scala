package amf.plugins.features.validation

import amf.ProfileNames
import amf.core.client.GenerationOptions
import amf.framework.model.document.{BaseUnit, Document}
import amf.framework.plugins.{AMFDocumentPlugin, AMFValidationPlugin}
import amf.framework.registries.AMFPluginsRegistry
import amf.framework.remote.Platform
import amf.framework.services.{RuntimeCompiler, RuntimeSerializer, RuntimeValidator}
import amf.framework.validation.core.ValidationReport
import amf.framework.validation.{AMFValidationReport, EffectiveValidations, ParserSideValidationPlugin}
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.RAMLExtensionsPlugin
import amf.plugins.document.vocabularies.model.domain.DomainEntity
import amf.plugins.document.webapi.PayloadPlugin
import amf.validation._
import amf.validation.emitters.{JSLibraryEmitter, ValidationJSONLDEmitter}
import amf.validation.model.ValidationProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFValidatorPlugin(platform: Platform) extends ParserSideValidationPlugin {

  override val ID = "AMF Validation"

  override def dependencies() = Seq(AMFGraphPlugin, PayloadPlugin)

  val url = "http://raml.org/dialects/profile.raml"

  // All the profiles are collected here, plugins can generate their own profiles
  def  profiles: Map[String, () => ValidationProfile] = AMFPluginsRegistry.documentPlugins.foldLeft(Map[String, () => ValidationProfile]()) {
    case (acc, domainPlugin: AMFValidationPlugin) => acc ++ domainPlugin.domainValidationProfiles(platform)
    case (acc, _)                                 => acc
  } ++ customValidationProfiles

  // Mapping from profile to domain plugin
  def  profilesPlugins: Map[String, AMFDocumentPlugin] = AMFPluginsRegistry.documentPlugins.foldLeft(Map[String,AMFDocumentPlugin]()) {
    case (acc, domainPlugin: AMFValidationPlugin) => acc ++ domainPlugin.domainValidationProfiles(platform).keys.foldLeft(Map[String, AMFDocumentPlugin]()) {
      case (accProfiles, profileName) => accProfiles.updated(profileName, domainPlugin)
    }
    case (acc, _)  => acc
  } ++ customValidationProfilesPlugins

  var customValidationProfiles: Map[String, ()=> ValidationProfile]= Map.empty
  var customValidationProfilesPlugins: Map[String, AMFDocumentPlugin]= Map.empty

  override def loadValidationProfile(validationProfilePath: String): Future[Unit] = {
    val currentValidation = new Validation(platform).withEnabledValidation(false)
    RuntimeCompiler(
      validationProfilePath,
      platform,
      "application/yaml",
      RAMLExtensionsPlugin.ID,
      currentValidation
    ).map { case parsed: Document => parsed.encodes }
      .map {
        case encoded: DomainEntity if encoded.definition.shortName == "Profile" =>
          val profile = ValidationProfile(encoded)
          val domainPlugin = profilesPlugins.get(profile.name) match {
            case Some(plugin) => {
              plugin
            }
            case None         => {
              profilesPlugins.get(profile.baseProfileName.getOrElse("AMF")) match {
                case Some(plugin) => {
                  plugin
                }
                case None         => {
                  throw new Exception(s"Plugin for custom validation profile ${profile.name}, ${profile.baseProfileName} not found")
                }
              }
            }
          }
          customValidationProfiles += (profile.name -> {() => profile })
          customValidationProfilesPlugins += (profile.name -> domainPlugin)
      }
  }

  def computeValidations(profileName: String,
                                  computed: EffectiveValidations = new EffectiveValidations()): EffectiveValidations = {
    val maybeProfile = profiles.get(profileName) match {
      case Some(profileGenerator) => Some(profileGenerator())
      case _                      => None
    }

    maybeProfile match {
      case Some(foundProfile) =>
        if (foundProfile.baseProfileName.isDefined) {
          computeValidations(foundProfile.baseProfileName.get, computed).someEffective(foundProfile)
        } else {
          computed.someEffective(foundProfile)
        }
      case None          => computed
    }
  }

  override def shaclValidation(model: BaseUnit, validations: EffectiveValidations, messageStyle: String): Future[ValidationReport] = {
    // println(s"VALIDATIONS: ${validations.effective.values.size} / ${validations.all.values.size} => $profileName")
    // validations.effective.keys.foreach(v => println(s" - $v"))

    val shapesJSON = shapesGraph(validations, messageStyle)

    // TODO: Check the validation profile passed to JSLibraryEmitter, it contains the prefixes
    // for the functions
    val jsLibrary  = new JSLibraryEmitter(None).emitJS(validations.effective.values.toSeq)

    jsLibrary match {
      case Some(code) => platform.validator.registerLibrary(ValidationJSONLDEmitter.validationLibraryUrl, code)
      case _          => // ignore
    }

    val modelJSON = RuntimeSerializer(model, "application/ld+json", "AMF Graph", GenerationOptions())

    /*
    println("\n\nGRAPH")
    println(modelJSON.length)
    println("===========================")
    println("\n\nVALIDATION")
    println(shapesJSON.length)
    println("===========================")
    println(jsLibrary)
    println("===========================")
    */

    ValidationMutex.synchronized {
      platform.validator.report(
        modelJSON,
        "application/ld+json",
        shapesJSON,
        "application/ld+json"
      )
    }
  }

  override def validate(model: BaseUnit, profileName: String, messageStyle: String): Future[AMFValidationReport] = {
    super.validate(model, profileName, messageStyle) flatMap { parserSideValidation =>
      profilesPlugins.get(profileName) match {
        case Some(domainPlugin: AMFValidationPlugin) =>
          val validations = computeValidations(profileName)
          domainPlugin.validationRequest(model, profileName, validations, platform) map { modelValidations =>
            modelValidations.copy(
              conforms = modelValidations.conforms && parserSideValidation.conforms,
              results = modelValidations.results ++ parserSideValidation.results
            )
          }
        case _ => Future {
          profileNotFoundWarningReport(model, profileName)
        }
      }
    }
  }

  def profileNotFoundWarningReport(model: BaseUnit, profileName: String) = {
    AMFValidationReport(conforms = true, model.location, profileName, Seq())
  }

  /**
    * Generates a JSON-LD graph with the SHACL shapes for the requested profile validations
    * @return JSON-LD graph
    */
  def shapesGraph(validations: EffectiveValidations, messageStyle: String = ProfileNames.RAML): String = {
    new ValidationJSONLDEmitter(messageStyle).emitJSON(validations.effective.values.toSeq.filter(s =>
      !s.isParserSide()))
  }

}

object AMFValidatorPlugin {
  def init(platform: Platform) {
    // Registering ourselves as the runtime validator
    RuntimeValidator.register(new AMFValidatorPlugin(platform))
  }
}
