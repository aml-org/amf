package amf.plugins.features.validation

import amf.ProfileNames
import amf.client.render.RenderOptions
import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin, AMFValidationPlugin}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Context
import amf.core.services.{RuntimeCompiler, RuntimeSerializer, RuntimeValidator}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.core.{ValidationProfile, ValidationReport}
import amf.core.validation.{AMFValidationReport, EffectiveValidations}
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.vocabularies.model.document.DialectInstance
import amf.plugins.document.vocabularies.model.domain.DialectDomainElement
import amf.plugins.features.validation.emitters.{JSLibraryEmitter, ValidationJSONLDEmitter}
import amf.plugins.features.validation.model.{ParsedValidationProfile, ValidationDialectText}
import amf.plugins.syntax.SYamlSyntaxPlugin

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AMFValidatorPlugin extends ParserSideValidationPlugin with PlatformSecrets {

  override val ID = "AMF Validation"

  override def init(): Future[AMFPlugin] = {
    // Registering ourselves as the runtime validator
    RuntimeValidator.register(AMFValidatorPlugin)
    val url = "http://raml.org/dialects/profile.raml"
    ExecutionLog.log(s"AMFValidatorPlugin#init: registering validation dialect")
    RAMLVocabulariesPlugin.registry.registerDialect(url, ValidationDialectText.text) map { _ =>
      ExecutionLog.log(s"AMFValidatorPlugin#init: validation dialect registered")
      this
    }
  }

  override def dependencies() = Seq(SYamlSyntaxPlugin, RAMLVocabulariesPlugin, AMFGraphPlugin)

  val url = "http://raml.org/dialects/profile.raml"

  // All the profiles are collected here, plugins can generate their own profiles
  def profiles: Map[String, () => ValidationProfile] =
    AMFPluginsRegistry.documentPlugins.foldLeft(Map[String, () => ValidationProfile]()) {
      case (acc, domainPlugin: AMFValidationPlugin) => acc ++ domainPlugin.domainValidationProfiles(platform)
      case (acc, _)                                 => acc
    } ++ customValidationProfiles

  // Mapping from profile to domain plugin
  def profilesPlugins: Map[String, AMFDocumentPlugin] =
    AMFPluginsRegistry.documentPlugins.foldLeft(Map[String, AMFDocumentPlugin]()) {
      case (acc, domainPlugin: AMFValidationPlugin) =>
        acc ++ domainPlugin.domainValidationProfiles(platform).keys.foldLeft(Map[String, AMFDocumentPlugin]()) {
          case (accProfiles, profileName) => accProfiles.updated(profileName, domainPlugin)
        }
      case (acc, _) => acc
    } ++ customValidationProfilesPlugins

  var customValidationProfiles: Map[String, () => ValidationProfile]  = Map.empty
  var customValidationProfilesPlugins: Map[String, AMFDocumentPlugin] = Map.empty

  override def loadValidationProfile(validationProfilePath: String): Future[String] = {
    RuntimeCompiler(
      validationProfilePath,
      Some("application/yaml"),
      RAMLVocabulariesPlugin.ID,
      Context(platform)
    ).map {
        case parsed: DialectInstance if parsed.definedBy().is("http://raml.org/dialects/profile.raml#") =>
          parsed.encodes
        case _ =>
          throw new Exception(
            "Trying to load as a validation profile that does not match the Validation Profile dialect")
      }
      .map {
        case encoded: DialectDomainElement if encoded.definedBy.name.is("profileNode") =>
          val profile = ParsedValidationProfile(encoded)
          val domainPlugin = profilesPlugins.get(profile.name) match {
            case Some(plugin) => plugin
            case None =>
              profilesPlugins.get(profile.baseProfileName.getOrElse("AMF")) match {
                case Some(plugin) =>
                  plugin
                case None => RAMLVocabulariesPlugin

              }
          }
          customValidationProfiles += (profile.name -> { () =>
            profile
          })
          customValidationProfilesPlugins += (profile.name -> domainPlugin)
          profile.name

        case other =>
          throw new Exception(
            "Trying to load as a validation profile that does not match the Validation Profile dialect")
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
      case None => computed
    }
  }

  override def shaclValidation(model: BaseUnit,
                               validations: EffectiveValidations,
                               messageStyle: String): Future[ValidationReport] = {
    ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: shacl validation for ${validations.effective.values.size} validations")
    // println(s"VALIDATIONS: ${validations.effective.values.size} / ${validations.all.values.size} => $profileName")
    // validations.effective.keys.foreach(v => println(s" - $v"))

    val shapesJSON = shapesGraph(validations, messageStyle)

    ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: shapes graph JSON generated")

    // TODO: Check the validation profile passed to JSLibraryEmitter, it contains the prefixes
    // for the functions
    val jsLibrary = new JSLibraryEmitter(None).emitJS(validations.effective.values.toSeq)

    jsLibrary match {
      case Some(code) => PlatformValidator.instance.registerLibrary(ValidationJSONLDEmitter.validationLibraryUrl, code)
      case _          => // ignore
    }

    ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: jsLibrary generated")

    val modelJSON = RuntimeSerializer(model, "application/ld+json", "AMF Graph", RenderOptions().withoutSourceMaps)

    ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: data graph generated")
    /*
     println("\n\nGRAPH")
     println(modelJSON)
     println("===========================")
     println("\n\nVALIDATION")
     println(shapesJSON)
     println("===========================")
     println(jsLibrary)
     println("===========================")
     */

    ValidationMutex.synchronized {
      PlatformValidator.instance.report(
        modelJSON,
        "application/ld+json",
        shapesJSON,
        "application/ld+json"
      ).map { case report =>
        ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: validation finished")
        report
      }
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
        case _ =>
          Future {
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

object ValidationMutex {}
