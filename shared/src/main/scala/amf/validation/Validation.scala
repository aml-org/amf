package amf.validation

import amf.ProfileNames
import amf.client.GenerationOptions
import amf.compiler.AMFCompiler
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.LexicalInformation
import amf.domain.DomainElement
import amf.domain.dialects.DomainEntity
import amf.framework.validation.{AMFValidationReport, AMFValidationResult, EffectiveValidations, SeverityLevels}
import amf.model.AmfArray
import amf.plugins.document.graph.parser.GraphEmitter
import amf.plugins.document.webapi.validation.{AnnotationsValidation, ExamplesValidation, ShapeFacetsValidation}
import amf.remote.{Platform, RamlYamlHint}
import amf.spec.dialects.Dialect
import amf.framework.validation.core.{ValidationDialectText, ValidationResult}
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.validation.emitters.{JSLibraryEmitter, ValidationJSONLDEmitter}
import amf.validation.model._
import amf.vocabulary.Namespace
import org.yaml.render.JsonRender

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}





class Validation(platform: Platform) {

  val url = "http://raml.org/dialects/profile.raml"

  /**
    * Loads the validation dialect from the provided URL
    */
  def loadValidationDialect(): Future[Dialect] = {
    platform.dialectsRegistry.get("%Validation Profile 1.0") match {
      case Some(dialect) => Promise().success(dialect).future
      case None          => platform.dialectsRegistry.registerDialect(url, ValidationDialectText.text)
    }
  }

  var profile: Option[ValidationProfile] = None

  // The aggregated report
  def reset(): Unit = {
    aggregatedReport = List()
  }
  var aggregatedReport: List[AMFValidationResult] = List()

  // disable temporarily the reporting of validations
  var enabled: Boolean = true

  def withEnabledValidation(enabled: Boolean): Validation = {
    this.enabled = enabled
    this
  }

  def disableValidations[T]()(f: () => T): T = {
    if (enabled) {
      enabled = false
      try {
        f()
      } finally {
        enabled = true
      }
    } else {
      f()
    }
  }

  /**
    * Client code can use this function to register a new validation failure
    */
  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None): Unit = {
    val validationError = AMFValidationResult(message, level, targetNode, targetProperty, validationId, position)

    if (enabled) {
      aggregatedReport ++= Seq(validationError)
    } else {
      throw new Exception(validationError.toString)
    }
  }

  lazy val defaultProfiles: List[ValidationProfile] = DefaultAMFValidations.profiles()

  def loadValidationProfile(validationProfilePath: String): Future[Unit] = {
    val currentValidation = new Validation(platform).withEnabledValidation(false)
    AMFCompiler(validationProfilePath,
                platform,
                RamlYamlHint,
                currentValidation,
                None,
                None)
      .build()
      .map { case parsed: Document => parsed.encodes }
      .map {
        case encoded: DomainEntity if encoded.definition.shortName == "Profile" =>
          profile = Some(ValidationProfile(encoded))
      }
  }

  /**
    * Loads a validation profile generated out of a RAML Dialect
    * @param dialect RAML dialect to be parsed as a Validation Profile
    */
  def loadDialectValidationProfile(dialect: Dialect): Unit =
    profile = Some(new AMFDialectValidations(dialect).profile())

  /**
    * Returns the lsit of effective validations for the requested profile
    * @param profileName Name of the profile
    * @return list of effective validations: matching the profile and not filtered
    */
  def computeValidations(profileName: String,
                         computed: EffectiveValidations = new EffectiveValidations()): EffectiveValidations = {
    profileName match {
      case ProfileNames.AMF =>
        computed.someEffective(defaultProfiles.find(_.name == ProfileNames.AMF).get)
      case ProfileNames.RAML =>
        computeValidations(ProfileNames.AMF, computed).someEffective(defaultProfiles.find(_.name == ProfileNames.RAML).get)
      case ProfileNames.OAS =>
        computeValidations(ProfileNames.AMF, computed).someEffective(defaultProfiles.find(_.name == ProfileNames.OAS).get)
      case _ if platform.dialectsRegistry.knowsHeader("%" + profileName) =>
        val dialectValidationProfile =
          new AMFDialectValidations(platform.dialectsRegistry.get("%" + profileName).get).profile()
        computed.someEffective(dialectValidationProfile)
      case _ if profile.isDefined && profile.get.name == "Payload" =>
        computed.allEffective(profile.get.validations)
      case _ if profile.isDefined && profile.get.name == profileName =>
        if (profile.get.baseProfileName.isDefined) {
          computeValidations(profile.get.baseProfileName.get, computed).someEffective(profile.get)
        } else {
          computed
            .someEffective(DefaultAMFValidations.parserSideValidationsProfile)
            .someEffective(profile.get)
        }
      case _ => throw new Exception(s"Validation profile $profileName not defined")
    }
  }

  /**
    * Generates a JSON-LD graph with the SHACL shapes for the requested profile validations
    * @return JSON-LD graph
    */
  def shapesGraph(validations: EffectiveValidations, messageStyle: String = ProfileNames.RAML): String = {
    new ValidationJSONLDEmitter(messageStyle).emitJSON(validations.effective.values.toSeq.filter(s =>
      !s.isParserSide()))
  }

  def validate(model: BaseUnit,
               profileName: String,
               messageStyle: String = ProfileNames.RAML): Future[AMFValidationReport] = {
    val graphAST    = GraphEmitter.emit(model, GenerationOptions())
    val modelJSON   = JsonRender.render(graphAST)
    val validations = computeValidations(profileName)
    // println(s"VALIDATIONS: ${validations.effective.values.size} / ${validations.all.values.size} => $profileName")
    // validations.effective.keys.foreach(v => println(s" - $v"))
    val shapesJSON = shapesGraph(validations, messageStyle)
    val jsLibrary  = new JSLibraryEmitter(profile).emitJS(validations.effective.values.toSeq)

    val test = new AMFValidatorPlugin(platform)

    /*
    if (profileName == ProfileNames.RAML) {
      println("\n\nGRAPH")
      println(modelJSON)
      println("===========================")
      println("\n\nVALIDATION")
      println(shapesJSON)
      println("===========================")
      println(jsLibrary)
      println("===========================")
    }
    */

    jsLibrary match {
      case Some(code) => platform.validator.registerLibrary(ValidationJSONLDEmitter.validationLibraryUrl, code)
      case _          => // ignore
    }

    for {

      examplesReport <- if (profile.getOrElse("") != "Payload") {
        ExamplesValidation(model, platform).validate()
      } else {
        Promise[Seq[AMFValidationResult]]().success(Seq()).future
      }

      shapeFacetsReport <- if (profile.getOrElse("") != "Payload") {
        ShapeFacetsValidation(model, platform).validate()
      } else {
        Promise[Seq[AMFValidationResult]]().success(Seq()).future
      }

      annotationsReport <- if (profile.getOrElse("") != "Payload") {
        AnnotationsValidation(model, platform).validate()
      } else {
        Promise[Seq[AMFValidationResult]]().success(Seq()).future
      }

      shaclReport <- ValidationMutex.synchronized {
        platform.validator.report(
          modelJSON,
          "application/ld+json",
          shapesJSON,
          "application/ld+json"
        )
      }

    } yield {
      // aggregating parser-side validations
      var results = aggregatedReport.map(r => processAggregatedResult(r, messageStyle, validations))

      // adding model-side validations
      results ++= shaclReport.results
        .map(r => buildValidationForProfile(profileName, model, r, messageStyle, validations))
        .filter(_.isDefined)
        .map(_.get)

      // adding example validations
      results ++= examplesReport
        .map(r => buildValidationWithCustomLevelForProfile(profileName, model, r, messageStyle, validations))
        .filter(_.isDefined)
        .map(_.get)

      // adding shape facets validations
      results ++= shapeFacetsReport
        .map(r => buildValidationWithCustomLevelForProfile(profileName, model, r, messageStyle, validations))
        .filter(_.isDefined)
        .map(_.get)

      // adding annotations validations
      results ++= annotationsReport
        .map(r => buildValidationWithCustomLevelForProfile(profileName, model, r, messageStyle, validations))
        .filter(_.isDefined)
        .map(_.get)

      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = model.id,
        profile = profileName,
        results = results
      )
    }
  }

  protected def buildValidationForProfile(profileName: String,
                                          model: BaseUnit,
                                          r: ValidationResult,
                                          messageStyle: String,
                                          validations: EffectiveValidations): Option[AMFValidationResult] = {
    profileName match {
      case "Payload" => buildPayloadValidationResult(model, r, validations)
      case _         => buildValidationResult(model, r, messageStyle, validations)
    }
  }

  def buildValidationWithCustomLevelForProfile(profileName: String,
                                               model: BaseUnit,
                                               result: AMFValidationResult,
                                               messageStyle: String,
                                               validations: EffectiveValidations): Option[AMFValidationResult] = {
    profileName match {
      case "Payload" => None
      case _         => Some(result.copy(level = findLevel(result.validationId, validations)))
    }
  }

  protected def findLevel(id: String, validations: EffectiveValidations): String = {
    if (validations.info.get(id).isDefined) {
      SeverityLevels.INFO
    } else if (validations.warning.get(id).isDefined) {
      SeverityLevels.WARNING
    } else {
      SeverityLevels.VIOLATION
    }
  }

  protected def processAggregatedResult(result: AMFValidationResult,
                                        messageStyle: String,
                                        validations: EffectiveValidations): AMFValidationResult = {
    val spec = validations.all.get(result.validationId) match {
      case Some(s) => s
      case None    => throw new Exception(s"Cannot find spec for aggregated validation result ${result.validationId}")
    }

    var message: String = messageStyle match {
      case ProfileNames.RAML => spec.ramlMessage.getOrElse(result.message)
      case ProfileNames.OAS  => spec.oasMessage.getOrElse(result.message)
      case _                 => spec.message
    }
    if (message == "") {
      message = "Constraint violation"
    }

    val severity = findLevel(spec.id(), validations)
    new AMFValidationResult(message, severity, result.targetNode, result.targetProperty, spec.id(), result.position)
  }

  protected def buildValidationResult(model: BaseUnit,
                                      result: ValidationResult,
                                      messageStyle: String,
                                      validations: EffectiveValidations): Option[AMFValidationResult] = {
    val validationSpecToLook = if (result.sourceShape.startsWith(Namespace.Data.base)) {
      result.sourceShape
        .replace(Namespace.Data.base, "") // this is for custom validations they are all prefixed with the data namespace
    } else {
      result.sourceShape // by default we expect to find a URI here
    }
    val idMapping: mutable.HashMap[String, String] = mutable.HashMap()
    val maybeTargetSpec: Option[ValidationSpecification] = validations.all.get(validationSpecToLook) match {
      case Some(validationSpec) =>
        idMapping.put(result.sourceShape, validationSpecToLook)
        Some(validationSpec)

      case None =>
        validations.all.find {
          case (v, _) =>
            // processing property shapes Id computed as constraintID + "/prop"

            validationSpecToLook.startsWith(v)
        } match {
          case Some((v, spec)) =>
            idMapping.put(result.sourceShape, v)
            Some(spec)
          case None =>
            if (validationSpecToLook.startsWith("_:")) {
              None
            } else {
              throw new Exception(s"Cannot find validation spec for validation error:\n $result")
            }
        }
    }

    maybeTargetSpec match {
      case Some(targetSpec) =>
        var message = messageStyle match {
          case ProfileNames.RAML => targetSpec.ramlMessage.getOrElse(targetSpec.message)
          case ProfileNames.OAS  => targetSpec.ramlMessage.getOrElse(targetSpec.message)
          case _                 => Option(targetSpec.message).getOrElse(result.message.getOrElse(""))
        }

        if (Option(message).isEmpty || message == "") {
          message = result.message.getOrElse("Constraint violation")
        }

        val finalId = idMapping(result.sourceShape).startsWith("http") match {
          case true => idMapping(result.sourceShape)
          case false =>
            Namespace.Data.base + idMapping(result.sourceShape) // we put back the prefix for the custom validations
        }
        val severity = findLevel(idMapping(result.sourceShape), validations)
        Some(
          AMFValidationResult.withShapeId(finalId,
                                          AMFValidationResult.fromSHACLValidation(model, message, severity, result)))
      case _ => None
    }
  }

  protected def buildPayloadValidationResult(model: BaseUnit,
                                             result: ValidationResult,
                                             validations: EffectiveValidations): Option[AMFValidationResult] = {
    val validationSpecToLook = if (result.sourceShape.startsWith(Namespace.Data.base)) {
      result.sourceShape
        .replace(Namespace.Data.base, "") // this is for custom validations they are all prefixed with the data namespace
    } else {
      result.sourceShape // by default we expect to find a URI here
    }
    val maybeTargetSpec: Option[ValidationSpecification] = validations.all.get(validationSpecToLook) match {
      case Some(validationSpec) =>
        Some(validationSpec)

      case None =>
        validations.all.find {
          case (v, validation) =>
            // processing property shapes Id computed as constraintID + "/prop"
            validation.propertyConstraints.find(p => p.name == validationSpecToLook) match {
              case Some(p) => true
              case None    => validationSpecToLook.startsWith(v)
            }
        } match {
          case Some((v, spec)) =>
            Some(spec)
          case None =>
            if (validationSpecToLook.startsWith("_:")) {
              None
            } else {
              throw new Exception(s"Cannot find validation spec for validation error:\n $result")
            }
        }
    }

    maybeTargetSpec match {
      case Some(targetSpec) =>
        val propertyConstraint = targetSpec.propertyConstraints.find(p => p.name == validationSpecToLook)

        var message = propertyConstraint match {
          case Some(p) => p.message.getOrElse(targetSpec.message)
          case None    => targetSpec.message
        }

        if (Option(message).isEmpty || message == "") {
          message = result.message.getOrElse("Constraint violation")
        }

        val finalId = propertyConstraint match {
          case Some(p) => p.name
          case None    => targetSpec.name
        }
        val severity = SeverityLevels.VIOLATION
        Some(
          AMFValidationResult.withShapeId(finalId,
                                          AMFValidationResult.fromSHACLValidation(model, message, severity, result)))
      case _ => None
    }
  }
}

object ValidationMutex {}
object Validation {
  def apply(platform: Platform) = new Validation(platform)
}
