package amf.plugins.features.validation

import amf.client.GenerationOptions
import amf.core.AMFPluginsRegistry
import amf.document.{BaseUnit, Document}
import amf.domain.dialects.DomainEntity
import amf.framework.plugins.{AMFDomainPlugin, AMFValidationPlugin}
import amf.framework.services.{RuntimeCompiler, RuntimeSerializer, RuntimeValidator}
import amf.framework.validation.{AMFValidationReport, EffectiveValidations}
import amf.plugins.document.vocabularies.RAMLExtensionsPlugin
import amf.remote.Platform
import amf.validation.model.ValidationProfile
import amf.validation._
import amf.framework.validation.core.ValidationReport
import amf.validation.emitters.{JSLibraryEmitter, ValidationJSONLDEmitter}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AMFValidatorPlugin(platform: Platform) extends Validation(platform) with RuntimeValidator {

  // Registering ourselves as the runtime validator
  RuntimeValidator.register(this)

  // All the profiles are collected here, plugins can generate their own profiles
  def  profiles: Map[String, () => ValidationProfile] = AMFPluginsRegistry.domainPlugins.foldLeft(Map[String, () => ValidationProfile]()) {
    case (acc, domainPlugin: AMFValidationPlugin) => acc ++ domainPlugin.domainValidationProfiles(platform)
    case (acc, _)                                 => acc
  } ++ customValidationProfiles

  // Mapping from profile to domain plugin
  def  profilesPlugins: Map[String, AMFDomainPlugin] = AMFPluginsRegistry.domainPlugins.foldLeft(Map[String,AMFDomainPlugin]()) {
    case (acc, domainPlugin: AMFValidationPlugin) => acc ++ domainPlugin.domainValidationProfiles(platform).keys.foldLeft(Map[String, AMFDomainPlugin]()) {
      case (accProfiles, profileName) => accProfiles.updated(profileName, domainPlugin)
    }
    case (acc, _)  => acc
  }

  var customValidationProfiles: Map[String, ()=> ValidationProfile]= Map.empty
  var customValidationProfilesPlugins: Map[String, AMFDomainPlugin]= Map.empty

  override def loadValidationProfile(validationProfilePath: String): Future[Unit] = {
    val currentValidation = new Validation(platform).withEnabledValidation(false)
    RuntimeCompiler(
      url,
      platform,
      "application/yaml",
      RAMLExtensionsPlugin.ID,
      currentValidation
    ).map { case parsed: Document => parsed.encodes }
      .map {
        case encoded: DomainEntity if encoded.definition.shortName == "Profile" =>
          val profile = ValidationProfile(encoded)
          val domainPlugin = profilesPlugins.get(profile.name) match {
            case Some(plugin) => plugin
            case None         => profilesPlugins.get(profile.baseProfileName.getOrElse("AMF")) match {
              case Some(plugin) => plugin
              case None         => throw new Exception(s"Plugin for custom validation profile ${profile.name}, ${profile.baseProfileName} not found")
            }
          }
          customValidationProfiles += (profile.name -> {() => profile })
          customValidationProfilesPlugins += (profile.name -> domainPlugin)
      }
  }

  override def computeValidations(profileName: String,
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
    val shapesJSON = shapesGraph(validations, messageStyle)

    // TODO: Check the validation profile passed to JSLibraryEmitter, it contains the prefixes
    // for the functions
    val jsLibrary  = new JSLibraryEmitter(None).emitJS(validations.effective.values.toSeq)

    jsLibrary match {
      case Some(code) => platform.validator.registerLibrary(ValidationJSONLDEmitter.validationLibraryUrl, code)
      case _          => // ignore
    }

    val modelJSON = RuntimeSerializer(model, "application/ld+json", "AMF Graph", GenerationOptions())


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
    val validations = computeValidations(profileName)

    profilesPlugins.get(profileName) match {
      case Some(domainPlugin: AMFValidationPlugin) =>
        val validations = computeValidations(profileName)
        domainPlugin.validationRequest(model, profileName, validations, platform)
      case _ => Future { profileNotFoundWarningReport(model, profileName) }
    }
  }

  def profileNotFoundWarningReport(model: BaseUnit, profileName: String) = {
    AMFValidationReport(conforms = true, model.location, profileName, Seq())
  }

}
