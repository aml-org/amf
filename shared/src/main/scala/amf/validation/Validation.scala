package amf.validation

import amf.client.GenerationOptions
import amf.compiler.AMFCompiler
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.LexicalInformation
import amf.domain.DomainElement
import amf.domain.dialects.DomainEntity
import amf.generator.JsonGenerator
import amf.graph.GraphEmitter
import amf.remote.{Platform, RamlYamlHint}
import amf.spec.dialects.Dialect
import amf.validation.core.ValidationResult
import amf.validation.model.{DefaultAMFValidations, JSONLDEmitter, ValidationProfile, ValidationSpecification}
import amf.vocabulary.Namespace

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ValidationProfileNames {
  val AMF  = "AMF"
  val OAS  = "OpenAPI"
  val RAML = "RAML"
}

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

  def fromSHACLValidation(model: BaseUnit, message: String, level: String, validation: ValidationResult): AMFValidationResult = {
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
    AMFValidationResult(validation.message, validation.level, validation.targetNode, validation.targetProperty, shapeId, validation.position)

  def findPosition(node: DomainElement, validation: ValidationResult): Option[LexicalInformation] = {
    if (validation.path != null) {
      val foundPosition = node.fields.fields().find(f => f.field.value.iri() == validation.path) match {
        case Some(f) =>
          f.element.annotations.find(classOf[LexicalInformation])
        case _ => None
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
    results.groupBy(_.level) foreach { case (level, results) =>
      str += s"\nLevel: $level\n"
      for { result <- results } {
        str += result
      }
    }
    str
  }
}

class EffectiveValidations(val effective: mutable.HashMap[String,ValidationSpecification] = mutable.HashMap(),
                           val info: mutable.HashMap[String,ValidationSpecification] = mutable.HashMap(),
                           val warning: mutable.HashMap[String,ValidationSpecification] = mutable.HashMap(),
                           val violation: mutable.HashMap[String,ValidationSpecification] = mutable.HashMap(),
                           val all: mutable.HashMap[String,ValidationSpecification] = mutable.HashMap())

object SeverityLevels {
  val WARNING = "Warning"
  val INFO = "Info"
  val VIOLATION = "Violation"
}

class Validation(platform: Platform) {

  /**
    * Loads the validation dialect from the provided URL
    */
  def loadValidationDialect(validationDialectUrl: String): Future[Dialect] = platform.dialectsRegistry.registerDialect(validationDialectUrl)


  var profile: Option[ValidationProfile] = None

  // The aggregated report
  def reset(): Unit = { aggregatedReport = List() }
  var aggregatedReport: List[AMFValidationResult] = List()

  /**
    * Client code can use this function to register a new validation failure
    * @param validation
    */
  def reportConstraintFailure(validation: AMFValidationResult): Unit = aggregatedReport ++= Seq(validation)

  lazy val defaultProfiles: List[ValidationProfile] = DefaultAMFValidations.profiles()

  def loadValidationProfile(validationProfilePath: String): Future[Unit] = {
    AMFCompiler(validationProfilePath, platform, RamlYamlHint, None, None, platform.dialectsRegistry)
      .build()
      .map { case parsed: Document => parsed.encodes }
      .map { case encoded: DomainEntity if encoded.definition.shortName == "Profile" =>
        profile = Some(ValidationProfile(encoded))
      }
  }

  private def setLevel(id: String, validations: EffectiveValidations, targetLevel: String) = {
    val validationName = if (!id.startsWith("http://") && !id.startsWith("https://")) { Namespace.expand(id.replace(".",":")).iri() } else { id }
    validations.all.get(validationName) match {
      case None             => throw new Exception(s"Cannot enable with $targetLevel level unknown validation $validationName")
      case Some(validation) =>
        validations.info.remove(validationName)
        validations.warning.remove(validationName)
        validations.violation.remove(validationName)
        targetLevel match {
          case SeverityLevels.INFO      => validations.info += (validationName -> validation)
          case SeverityLevels.WARNING   => validations.warning += (validationName -> validation)
          case SeverityLevels.VIOLATION => validations.violation += (validationName -> validation)
        }
        validations.effective += (validationName -> validation)
    }
  }

  private def someEffective(profile: ValidationProfile, computed: EffectiveValidations): _root_.amf.validation.EffectiveValidations = {
    // we aggregate all of the validations to the total validations map
    profile.validations.foreach { spec => computed.all += spec.name -> spec}

    profile.infoLevel.foreach( id => setLevel(id, computed, SeverityLevels.INFO))
    profile.warningLevel.foreach( id => setLevel(id, computed, SeverityLevels.WARNING))
    profile.violationLevel.foreach( id => setLevel(id, computed, SeverityLevels.VIOLATION))

    profile.disabled foreach { id =>
      val validationName = if (!id.startsWith("http://") && !id.startsWith("https://")) { Namespace.expand(id.replace(".",":")).iri() } else { id }
      computed.effective.remove(validationName)
    }

    computed
  }

  private def allEffective(specifications: Seq[ValidationSpecification], validations: EffectiveValidations) = {
    specifications foreach { spec =>
      validations.all += (spec.name -> spec)
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
  def computeValidations(profileName: String, computed: EffectiveValidations = new EffectiveValidations()): EffectiveValidations = {
    profileName match {
      case ValidationProfileNames.AMF =>
        allEffective(defaultProfiles.find(_.name == ValidationProfileNames.AMF).get.validations, computed)
      case ValidationProfileNames.RAML =>
        allEffective(defaultProfiles.find(_.name == ValidationProfileNames.RAML).get.validations, computeValidations(ValidationProfileNames.AMF, computed))
      case ValidationProfileNames.OAS =>
        allEffective(defaultProfiles.find(_.name == ValidationProfileNames.OAS).get.validations, computeValidations(ValidationProfileNames.AMF, computed))
      case _ if profile.isDefined && profile.get.name == profileName=>
        if (profile.get.baseProfileName.isDefined) {
          someEffective(profile.get, computeValidations(profile.get.baseProfileName.get, computed))
        } else {
          someEffective(profile.get, computeValidations(ValidationProfileNames.AMF, computed))
        }
      case _ => throw new Exception(s"Validation profile $profileName not defined")
    }
  }


  /**
    * Generates a JSON-LD graph with the SHACL shapes for the requested profile validations
    * @return JSON-LD graph
    */
  def shapesGraph(validations: EffectiveValidations, messageStyle: String = ValidationProfileNames.RAML): String = {
    new JSONLDEmitter(messageStyle).emitJSON(validations.effective.values.toSeq)
  }
  def validate(model: BaseUnit, profileName: String, messageStyle: String = ValidationProfileNames.RAML): Future[AMFValidationReport] = {
    val graphAST  = GraphEmitter.emit(model, GenerationOptions())
    val modelJSON = new JsonGenerator().generate(graphAST).toString
    val validations = computeValidations(profileName)
    // println(s"VALIDATIONS: ${validations.effective.values.size} / ${validations.all.values.size} => $profileName")
    // validations.effective.keys.foreach(v => println(s" - $v"))
    val shapesJSON = shapesGraph(validations, messageStyle)

    /*
    println("\n\nGRAPH")
    println(modelJSON)
    println("===========================")
    println("\n\nVALIDATION")
    println(shapesJSON)
    println("===========================")
    */

    for {
      shaclReport <- platform.validator.report(
        modelJSON, "application/ld+json",
        shapesJSON, "application/ld+json",
      )
    } yield {
      val results = shaclReport.results.map(r => buildValidationResult(model, r, messageStyle, validations))
      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = model.id,
        profile = profileName,
        results = results
      )
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

  protected def buildValidationResult(model: BaseUnit, result: ValidationResult, messageStyle: String, validations: EffectiveValidations): AMFValidationResult = {
    val validationSpecToLook = if (result.sourceShape.startsWith(Namespace.Data.base)) {
      result.sourceShape.replace(Namespace.Data.base, "") // this is for custom validations they are all prefixed with the data namespace
    } else {
      result.sourceShape // by default we expect to find a URI here
    }
    val idMapping: mutable.HashMap[String,String] = mutable.HashMap()
    val targetSpec = validations.all.get(validationSpecToLook) match {
      case Some(validationSpec) =>
        idMapping.put(result.sourceShape, validationSpecToLook)
        validationSpec

      case None => validations.all.find { case (v, _) =>
        validationSpecToLook.startsWith(v)
      } match {
        case Some((v, spec)) =>
          idMapping.put(result.sourceShape, v)
          spec
        case None => throw new Exception(s"Cannot find validation spec for validation error:\n $result")
      }
    }

    var message = messageStyle match {
      case ValidationProfileNames.RAML => targetSpec.ramlMessage.getOrElse(targetSpec.message)
      case ValidationProfileNames.OAS  => targetSpec.ramlMessage.getOrElse(targetSpec.message)
      case _                           => Option(targetSpec.message).getOrElse(result.message.getOrElse(""))
    }

    if (Option(message).isEmpty || message == "") {
      message = result.message.getOrElse("Constraint violation")
    }

    val finalId = idMapping(result.sourceShape)
    val severity = findLevel(finalId, validations)
    AMFValidationResult.withShapeId(finalId, AMFValidationResult.fromSHACLValidation(model, message, severity, result))
  }
}

object Validation {
  def apply(platform: Platform): Validation = new Validation(platform)
}