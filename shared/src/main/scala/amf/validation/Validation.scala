package amf.validation

import amf.ProfileNames
import amf.client.GenerationOptions
import amf.compiler.AMFCompiler
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.LexicalInformation
import amf.domain.DomainElement
import amf.domain.dialects.DomainEntity
import amf.graph.GraphEmitter
import amf.model.AmfArray
import amf.remote.{Platform, RamlYamlHint}
import amf.spec.dialects.Dialect
import amf.validation.core.{ValidationDialectText, ValidationResult}
import amf.validation.emitters.{JSLibraryEmitter, ValidationJSONLDEmitter}
import amf.validation.model._
import amf.vocabulary.Namespace
import org.yaml.render.JsonRender

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

case class AMFValidationResult(message: String,
                               level: String,
                               targetNode: String,
                               targetProperty: Option[String],
                               validationId: String,
                               position: Option[LexicalInformation]) {
  override def toString: String = {
    var str = s"\n- Source: $validationId\n"
    str += s"  Message: $message\n"
    str += s"  Level: $level\n"
    str += s"  Target: $targetNode\n"
    str += s"  Property: ${targetProperty.getOrElse("")}\n"
    str += s"  Position: $position\n"
    str
  }
}

object AMFValidationResult {

  def fromSHACLValidation(model: BaseUnit,
                          message: String,
                          level: String,
                          validation: ValidationResult): AMFValidationResult = {
    model.findById(validation.focusNode) match {
      case None => throw new Exception(s"Cannot find node with validation error ${validation.focusNode}")
      case Some(node) =>
        val position = findPosition(node, validation)
        AMFValidationResult(
          message = message,
          level = level,
          targetNode = node.id,
          targetProperty = Option(validation.path),
          validation.sourceShape,
          position = position
        )
    }
  }

  def withShapeId(shapeId: String, validation: AMFValidationResult): AMFValidationResult =
    AMFValidationResult(validation.message,
                        validation.level,
                        validation.targetNode,
                        validation.targetProperty,
                        shapeId,
                        validation.position)

  def findPosition(node: DomainElement, validation: ValidationResult): Option[LexicalInformation] = {
    if (validation.path != null && validation.path != "") {
      val foundPosition = node.fields.fields().find(f => f.field.value.iri() == validation.path) match {
        case Some(f) =>
          f.element.annotations.find(classOf[LexicalInformation]).orElse {
            f.value.annotations.find(classOf[LexicalInformation]).orElse {
              f.element match {
                case arr:AmfArray if arr.values.nonEmpty => arr.values.head.annotations.find(classOf[LexicalInformation])
                case _ => node.annotations.find(classOf[LexicalInformation])
              }
            }
          }
        case _ => node.annotations.find(classOf[LexicalInformation])
      }
      foundPosition
    } else {
      node.annotations.find(classOf[LexicalInformation])
    }
  }

}

case class AMFValidationReport(conforms: Boolean, model: String, profile: String, results: Seq[AMFValidationResult]) {
  override def toString: String = {
    var str = s"Model: $model\n"
    str += s"Profile: $profile\n"
    str += s"Conforms? $conforms\n"
    str += s"Number of results: ${results.length}\n"
    results.groupBy(_.level) foreach {
      case (level, results) =>
        str += s"\nLevel: $level\n"
        for { result <- results } {
          str += result
        }
    }
    str
  }
}

class EffectiveValidations(val effective: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val info: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val warning: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val violation: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val all: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap())

object SeverityLevels {
  val WARNING   = "Warning"
  val INFO      = "Info"
  val VIOLATION = "Violation"
}

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

  /**
    * Client code can use this function to register a new validation failure
    */
  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None): Unit = {
    aggregatedReport ++= Seq(AMFValidationResult(message, level, targetNode, targetProperty, validationId, position))
  }

  lazy val defaultProfiles: List[ValidationProfile] = DefaultAMFValidations.profiles()

  def loadValidationProfile(validationProfilePath: String): Future[Unit] = {
    AMFCompiler(validationProfilePath, platform, RamlYamlHint, None, None, platform.dialectsRegistry)
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

  private def setLevel(id: String, validations: EffectiveValidations, targetLevel: String) = {
    val validationName = if (!id.startsWith("http://") && !id.startsWith("https://") && !id.startsWith("file:/")) {
      Namespace.expand(id.replace(".", ":")).iri()
    } else { id }
    validations.all.get(validationName) match {
      case None => throw new Exception(s"Cannot enable with $targetLevel level unknown validation $validationName")
      case Some(validation) =>
        validations.info.remove(validationName)
        validations.warning.remove(validationName)
        validations.violation.remove(validationName)
        targetLevel match {
          case SeverityLevels.INFO      => validations.info += (validationName      -> validation)
          case SeverityLevels.WARNING   => validations.warning += (validationName   -> validation)
          case SeverityLevels.VIOLATION => validations.violation += (validationName -> validation)
        }
        validations.effective += (validationName -> validation)
    }
  }

  private def someEffective(profile: ValidationProfile,
                            computed: EffectiveValidations): _root_.amf.validation.EffectiveValidations = {
    // we aggregate all of the validations to the total validations map
    profile.validations.foreach { spec =>
      computed.all += spec.name -> spec
    }

    profile.infoLevel.foreach(id => setLevel(id, computed, SeverityLevels.INFO))
    profile.warningLevel.foreach(id => setLevel(id, computed, SeverityLevels.WARNING))
    profile.violationLevel.foreach(id => setLevel(id, computed, SeverityLevels.VIOLATION))

    profile.disabled foreach { id =>
      val validationName = if (!id.startsWith("http://") && !id.startsWith("https://") && !id.startsWith("file:/")) {
        Namespace.expand(id.replace(".", ":")).iri()
      } else { id }
      computed.effective.remove(validationName)
    }

    computed
  }

  private def allEffective(specifications: Seq[ValidationSpecification], validations: EffectiveValidations) = {
    specifications foreach { spec =>
      validations.all += (spec.name       -> spec)
      validations.effective += (spec.name -> spec)
      validations.violation += (spec.name -> spec)
    }
    validations
  }

  /**
    * Returns the lsit of effective validations for the requested profile
    * @param profileName Name of the profile
    * @return list of effective validations: matching the profile and not filtered
    */
  def computeValidations(profileName: String,
                         computed: EffectiveValidations = new EffectiveValidations()): EffectiveValidations = {
    profileName match {
      case ProfileNames.AMF =>
        allEffective(defaultProfiles.find(_.name == ProfileNames.AMF).get.validations, computed)
      case ProfileNames.RAML =>
        allEffective(defaultProfiles.find(_.name == ProfileNames.RAML).get.validations,
                     computeValidations(ProfileNames.AMF, computed))
      case ProfileNames.OAS =>
        allEffective(defaultProfiles.find(_.name == ProfileNames.OAS).get.validations,
                     computeValidations(ProfileNames.AMF, computed))
      case _ if platform.dialectsRegistry.knowsHeader("%" + profileName) =>
        val dialectValidationProfile =
          new AMFDialectValidations(platform.dialectsRegistry.get("%" + profileName).get).profile()
        someEffective(dialectValidationProfile, computed)
      case _ if profile.isDefined && profile.get.name == "Payload" =>
        allEffective(profile.get.validations, computed)
      case _ if profile.isDefined && profile.get.name == profileName =>
        if (profile.get.baseProfileName.isDefined) {
          someEffective(profile.get, computeValidations(profile.get.baseProfileName.get, computed))
        } else {
          someEffective(profile.get, computeValidations(ProfileNames.AMF, computed))
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
    val jsLibrary  = new JSLibraryEmitter().emitJS(validations.effective.values.toSeq)

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

    jsLibrary match {
      case Some(code) => platform.validator.registerLibrary(ValidationJSONLDEmitter.validationLibraryUrl, code)
      case _          => // ignore
    }
    for {
      shaclReport <- platform.validator.report(
        modelJSON,
        "application/ld+json",
        shapesJSON,
        "application/ld+json"
      )
    } yield {
      val results = aggregatedReport.map(r => processAggregatedResult(r, messageStyle, validations)) ++
        shaclReport.results.map(r => buildValidationForProfile(profileName, model, r, messageStyle, validations)).filter(_.isDefined).map(_.get)
      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = model.id,
        profile = profileName,
        results = results
      )
    }
  }

  protected def buildValidationForProfile(profileName: String, model: BaseUnit, r: ValidationResult, messageStyle: String, validations: EffectiveValidations): Option[AMFValidationResult] = {
    profileName match {
      case "Payload" => buildPayloadValidationResult(model, r, validations)
      case _         => buildValidationResult(model, r, messageStyle, validations)
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

      case None => validations.all.find { case (v, _) =>
        // processing property shapes Id computed as constraintID + "/prop"

        validationSpecToLook.startsWith(v)
      } match {
        case Some((v, spec)) =>
          idMapping.put(result.sourceShape, v)
          Some(spec)
        case None => if (validationSpecToLook.startsWith("_:")) {
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
        Some(AMFValidationResult.withShapeId(finalId,AMFValidationResult.fromSHACLValidation(model, message, severity, result)))
      case _ => None
    }
  }

  protected def buildPayloadValidationResult(model: BaseUnit, result: ValidationResult, validations: EffectiveValidations): Option[AMFValidationResult] = {
    val validationSpecToLook = if (result.sourceShape.startsWith(Namespace.Data.base)) {
      result.sourceShape.replace(Namespace.Data.base, "") // this is for custom validations they are all prefixed with the data namespace
    } else {
      result.sourceShape // by default we expect to find a URI here
    }
    val maybeTargetSpec: Option[ValidationSpecification] = validations.all.get(validationSpecToLook) match {
      case Some(validationSpec) =>
        Some(validationSpec)

      case None => validations.all.find { case (v, validation) =>
        // processing property shapes Id computed as constraintID + "/prop"
        validation.propertyConstraints.find(p => p.name == validationSpecToLook) match {
          case Some(p) => true
          case None    => validationSpecToLook.startsWith(v)
        }
      } match {
        case Some((v, spec)) =>
          Some(spec)
        case None => if (validationSpecToLook.startsWith("_:")) {
          None
        }  else {
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
        Some(AMFValidationResult.withShapeId(finalId, AMFValidationResult.fromSHACLValidation(model, message, severity, result)))
      case _ => None
    }
  }
}

object Validation {
  var currentValidation: Option[Validation] = None

  def apply(platform: Platform): Validation = {
    currentValidation = Some(new Validation(platform))
    currentValidation.get
  }

  def apply[T](platform: Platform, current: Validation => Future[T]): Future[T] = {
    val validation = new Validation(platform)
    currentValidation = Some(validation)
    val result = current(validation)
    result.map(r => {
      currentValidation = None
      r
    })
  }

  /**
    * Client code can use this function to register a new validation failure
    */
  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None): Unit = currentValidation match {
    case Some(v) => v.reportConstraintFailure(level, validationId, targetNode, targetProperty, message, position)
    case None =>
      if (level == SeverityLevels.VIOLATION) {
        throw new Exception(
          s"Violation: $message at node $targetNode, property $targetProperty and position $position")
      }
  }

  def restartValidations() = currentValidation match {
    case Some(v) => v.reset()
    case _       => // ignore
  }

}
