package amf.plugins.features.validation

import amf.client.plugins.{AMFFeaturePlugin, AMFPlugin}
import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.rdf.RdfModel
import amf.core.services.{IgnoreValidationsMerger, RuntimeValidator, ValidationOptions, ValidationsMerger}
import amf.core.validation._
import amf.core.validation.core.{ValidationProfile, ValidationReport, ValidationResult, ValidationSpecification}
import amf.internal.environment.Environment
import amf.{MessageStyle, ProfileName, ProfileNames}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ParserSideValidationPlugin extends AMFFeaturePlugin with RuntimeValidator with ValidationResultProcessor {

  override val ID: String = "Parser side AMF Validation"

  override def init(): Future[AMFPlugin] = Future {
    RuntimeValidator.validatorOption match {
      case Some(_) => // ignore, we use whatever has already been initialised by client code
      case None    => RuntimeValidator.register(new ParserSideValidationPlugin())
    }
    this
  }

  def parserSideValidationsProfile(profile: ProfileName): ValidationProfile = {
    // sorting parser side validation for this profile
    val violationParserSideValidations = Validations.validations
      .filter { v =>
        Validations
          .level(v.id, profile) == SeverityLevels.VIOLATION
      }
      .map(_.name)
    val infoParserSideValidations = Validations.validations
      .filter { v =>
        Validations
          .level(v.id, profile) == SeverityLevels.INFO
      }
      .map(_.name)
    val warningParserSideValidations = Validations.validations
      .filter { v =>
        Validations
          .level(v.id, profile) == SeverityLevels.WARNING
      }
      .map(_.name)

    ValidationProfile(
      name = ProfileName(ID),
      baseProfile = None,
      infoLevel = infoParserSideValidations,
      warningLevel = warningParserSideValidations,
      violationLevel = violationParserSideValidations,
      validations = Validations.validations
    )
  }

  override def dependencies(): Seq[AMFPlugin] = Seq()

  var aggregatedReport: Map[Int, ListBuffer[AMFValidationResult]] = Map()
  // disable temporarily the reporting of validations
  var enabled: Boolean = true

  // The aggregated report
  def reset(): Unit = {
    enabled = true
    aggregatedReport = Map()
  }

  def withEnabledValidation(enabled: Boolean): RuntimeValidator = {
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

  def disableValidationsAsync[T]()(f: (() => Unit) => T): T = {
    if (enabled) {
      enabled = false
      try {
        f(() => enabled = true)
      } catch {
        case e: Exception =>
          enabled = true
          throw e
      }
    } else {
      f(() => {})
    }
  }

  /**
    * Loads a validation profile from a URL
    */
  override def loadValidationProfile(validationProfilePath: String,
                                     env: Environment = Environment()): Future[ProfileName] =
    Future { ProfileName(ID) }

  /**
    * Low level validation returning a SHACL validation report
    */
  override def shaclValidation(model: BaseUnit,
                               validations: EffectiveValidations,
                               options: ValidationOptions): Future[ValidationReport] = Future {
    new ValidationReport {
      override def conforms: Boolean               = false
      override def results: List[ValidationResult] = Nil
    }
  }

  /**
    * Main validation function returning an AMF validation report linking validation errors
    * for validations in the profile to domain elements in the model
    */
  override def validate(model: BaseUnit,
                        profileName: ProfileName,
                        messageStyle: MessageStyle,
                        env: Environment): Future[AMFValidationReport] =
    aggregateReport(model, profileName, messageStyle)

  final def aggregateReport(model: BaseUnit,
                            profileName: ProfileName,
                            messageStyle: MessageStyle): Future[AMFValidationReport] = {
    val validations = EffectiveValidations().someEffective(parserSideValidationsProfile(profileName))
    // aggregating parser-side validations
    val results = model.parserRun match {
      case Some(runId) =>
        aggregatedReport.getOrElse(runId, Nil).map(r => processAggregatedResult(r, messageStyle, validations))
      case _ => Nil
    }

    Future {
      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = model.id,
        profile = profileName,
        results = results
      )
    }
  }

  /**
    * Client code can use this function to register a new validation failure
    */
  override def reportConstraintFailure(level: String,
                                       validationId: String,
                                       targetNode: String,
                                       targetProperty: Option[String] = None,
                                       message: String = "",
                                       position: Option[LexicalInformation] = None,
                                       parserRun: Int,
                                       location: Option[String]): Unit = synchronized {
    val validationError =
      AMFValidationResult(message, level, targetNode, targetProperty, validationId, position, location, this)
    if (enabled) {
      aggregatedReport.get(parserRun) match {
        case Some(validations) => validations += validationError
        case None =>
          aggregatedReport += (parserRun -> ListBuffer(validationError))
      }
    } else {
      if (level == SeverityLevels.VIOLATION)
        throw new Exception(validationError.toString)
    }
  }

  override def nestedValidation[T](merger: ValidationsMerger)(k: => T): T = {
    val oldAggregatedReport = aggregatedReport.getOrElse(merger.parserRun, ListBuffer())
    val oldEnabled          = enabled

    // reset
    enabled = true
    aggregatedReport = aggregatedReport.updated(merger.parserRun, ListBuffer())

    val res = k

    // undo reset
    if (merger.parserRun == IgnoreValidationsMerger.parserRun) {
      aggregatedReport = aggregatedReport.updated(merger.parserRun, ListBuffer()) // clean the ignore merger validations
    } else {
      val toMerge = aggregatedReport.getOrElse(merger.parserRun, ListBuffer()).filter(merger.merge)
      aggregatedReport = aggregatedReport.updated(merger.parserRun, oldAggregatedReport ++ toMerge)
      enabled = oldEnabled
    }

    res
  }

  /**
    * Returns a native RDF model with the SHACL shapes graph
    */
  override def shaclModel(validations: Seq[ValidationSpecification],
                          functionUrls: String,
                          messgeStyle: MessageStyle): RdfModel =
    throw new Exception("SHACL Support not available")

  override def emitShapesGraph(profileName: ProfileName): String =
    throw new Exception("SHACL Support not available")
}
