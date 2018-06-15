package amf.plugins.features.validation

import amf.ProfileNames
import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin, AMFValidationPlugin}
import amf.core.rdf.RdfModel
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Context
import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.core.{ValidationProfile, ValidationReport, ValidationSpecification}
import amf.core.validation.{AMFValidationReport, AMFValidationResult, EffectiveValidations}
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.graph.parser.ScalarEmitter
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.DialectInstance
import amf.plugins.document.vocabularies.model.domain.DialectDomainElement
import amf.plugins.features.validation.emitters.{JSLibraryEmitter, ValidationJSONLDEmitter}
import amf.plugins.features.validation.model.{ParsedValidationProfile, ValidationDialectText}
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AMFValidatorPlugin extends ParserSideValidationPlugin with PlatformSecrets {

  override val ID = "AMF Validation"

  override def init(): Future[AMFPlugin] = {
    // Registering ourselves as the runtime validator
    RuntimeValidator.register(AMFValidatorPlugin)
    ExecutionLog.log("Register RDF framework")
    platform.rdfFramework = Some(PlatformValidator.instance)
    ExecutionLog.log(s"AMFValidatorPlugin#init: registering validation dialect")
    AMLPlugin.registry.registerDialect(url, ValidationDialectText.text) map { _ =>
      ExecutionLog.log(s"AMFValidatorPlugin#init: validation dialect registered")
      this
    }
  }

  override def dependencies() = Seq(SYamlSyntaxPlugin, AMLPlugin, AMFGraphPlugin)

  val url = "http://a.ml/dialects/profile.raml"

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
      AMLPlugin.ID,
      Context(platform)
    ).map {
        case parsed: DialectInstance if parsed.definedBy().is(url + "#") =>
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
                case None => AMLPlugin

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

  object CustomScalarEmitter extends ScalarEmitter {
    override def scalar(b: PartBuilder, content: String, tag: YType, inArray: Boolean): Unit = {
      def emit(b: PartBuilder): Unit = {

        val tg: YType = fixTagIfNeeded(tag, content)

        b.obj { e =>
          forcedType(tag).foreach(t => e.entry("@type", t))
          e.entry("@value", raw(_, content, tg))
        }
      }

      if (inArray) emit(b) else b.list(emit)
    }

    private def forcedType(tag: YType): Option[String] = {
      tag match {
        case YType.Float => Some("http://www.w3.org/2001/XMLSchema#double")
        case _           => None
      }
    }
  }

  override def shaclValidation(model: BaseUnit,
                               validations: EffectiveValidations,
                               messageStyle: String): Future[ValidationReport] = {
    ExecutionLog.log(
      s"AMFValidatorPlugin#shaclValidation: shacl validation for ${validations.effective.values.size} validations")
    // println(s"VALIDATIONS: ${validations.effective.values.size} / ${validations.all.values.size} => $profileName")
    // validations.effective.keys.foreach(v => println(s" - $v"))

    // TODO: Check the validation profile passed to JSLibraryEmitter, it contains the prefixes
    // for the functions
    val jsLibrary = new JSLibraryEmitter(None).emitJS(validations.effective.values.toSeq)

    jsLibrary match {
      case Some(code) => PlatformValidator.instance.registerLibrary(ValidationJSONLDEmitter.validationLibraryUrl, code)
      case _          => // ignore
    }

    ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: jsLibrary generated")

    val data   = model
    val shapes = customValidations(validations)

    ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: Invoking platform validation")

    //ValidationMutex.synchronized {
      PlatformValidator.instance.report(data, shapes, messageStyle).map {
        case report =>
          ExecutionLog.log(s"AMFValidatorPlugin#shaclValidation: validation finished")
          report
      }
    //}
  }

  override def validate(model: BaseUnit, profileName: String, messageStyle: String): Future[AMFValidationReport] = {

    super.validate(model, profileName, messageStyle) flatMap {
      case parseSideValidation if !parseSideValidation.conforms => Future.successful(parseSideValidation)
      case parseSideValidation                                  => modelValidation(model, profileName, messageStyle, parseSideValidation.results)
    }

  }

  private def modelValidation(model: BaseUnit,
                              profileName: String,
                              messageStyle: String,
                              warningResults: Seq[AMFValidationResult]): Future[AMFValidationReport] = {
    profilesPlugins.get(profileName) match {
      case Some(domainPlugin: AMFValidationPlugin) =>
        val validations = computeValidations(profileName)
        domainPlugin
          .validationRequest(model, profileName, validations, platform)
          .map(a => a.copy(results = a.results ++ warningResults))
      case _ =>
        Future {
          profileNotFoundWarningReport(model, profileName)
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
    new ValidationJSONLDEmitter(messageStyle).emitJSON(customValidations(validations))
  }

  def customValidations(validations: EffectiveValidations): Seq[ValidationSpecification] =
    validations.effective.values.toSeq.filter(s => !s.isParserSide())

  /**
    * Returns a native RDF model with the SHACL shapes graph
    */
  override def shaclModel(validations: Seq[ValidationSpecification],
                          functionUrls: String,
                          messageStyle: String): RdfModel =
    PlatformValidator.instance.shapes(validations, functionUrls)

}

object ValidationMutex {}
