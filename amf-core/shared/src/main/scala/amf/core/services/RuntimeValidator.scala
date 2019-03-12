package amf.core.services

import amf.{AMFStyle, MessageStyle, ProfileName}
import amf.core.annotations.LexicalInformation
import amf.core.emitter.RenderOptions
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.rdf.RdfModel
import amf.core.validation.core.{ValidationReport, ValidationSpecification}
import amf.core.validation.{AMFValidationReport, AMFValidationResult, EffectiveValidations}
import amf.internal.environment.Environment

import scala.concurrent.Future

trait ValidationsMerger {
  val parserRun: Int
  def merge(result: AMFValidationResult): Boolean
}

object IgnoreValidationsMerger extends ValidationsMerger {
  override val parserRun: Int                              = -1
  override def merge(result: AMFValidationResult): Boolean = false
}

/**
  * Validation of AMF models
  */
trait RuntimeValidator {

  /**
    * Loads a validation profile from a URL
    */
  def loadValidationProfile(validationProfilePath: String, env: Environment = Environment()): Future[ProfileName]

  /**
    * Low level validation returning a SHACL validation report
    */
  def shaclValidation(model: BaseUnit,
                      validations: EffectiveValidations,
                      options: ValidationOptions): Future[ValidationReport]

  /**
    * Computes EffectiveValidations for validation profile
    */
  def computeValidations(profileName: ProfileName,
                         computed: EffectiveValidations = new EffectiveValidations()): EffectiveValidations

  /**
    * Generates a JSON-LD graph with the SHACL shapes for the requested profile validations
    * @return JSON-LD graph
    */
  def shapesGraph(validations: EffectiveValidations, profileName: ProfileName): String

  /**
    * Returns a native RDF model with the SHACL shapes graph
    */
  def shaclModel(validations: Seq[ValidationSpecification],
                 validationFunctionUrl: String,
                 messgeStyle: MessageStyle = AMFStyle): RdfModel

  /**
    * Main validation function returning an AMF validation report linking validation errors
    * for validations in the profile to domain elements in the model
    */
  def validate(model: BaseUnit,
               profileName: ProfileName,
               messageStyle: MessageStyle,
               env: Environment): Future[AMFValidationReport]

  def reset()

  def nestedValidation[T](merger: ValidationsMerger)(k: => T): T

  /**
    * Client code can use this function to register a new validation failure
    */
  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None,
                              parserRun: Int,
                              location: Option[String])

  /**
    * Temporary disable checking of runtime validations for the duration of the passed block
    */
  def disableValidations[T]()(f: () => T): T

  /**
    * Async version of disable valdiations
    */
  def disableValidationsAsync[T]()(f: (() => Unit) => T): T

  def aggregateReport(model: BaseUnit,
                      profileName: ProfileName,
                      messageStyle: MessageStyle): Future[AMFValidationReport]
}

object RuntimeValidator {
  var validatorOption: Option[RuntimeValidator] = None
  def register(runtimeValidator: RuntimeValidator): Unit = {
    validatorOption = Some(runtimeValidator)
  }

  private def validator: RuntimeValidator = {
    validatorOption match {
      case Some(runtimeValidator) => runtimeValidator
      case None                   => throw new Exception("No registered runtime validator")
    }
  }

  def loadValidationProfile(validationProfilePath: String,
                            env: Environment = Environment()): Future[ProfileName] =
    validator.loadValidationProfile(validationProfilePath, env)

  def shaclValidation(model: BaseUnit,
                      validations: EffectiveValidations,
                      options: ValidationOptions): Future[ValidationReport] =
    validator.shaclValidation(model, validations, options)

  def computeValidations(profileName: ProfileName,
                         computed: EffectiveValidations = new EffectiveValidations()): EffectiveValidations =
    validator.computeValidations(profileName)

  def shapesGraph(validations: EffectiveValidations, profileName: ProfileName): String =
    validator.shapesGraph(validations, profileName)

  def shaclModel(validations: Seq[ValidationSpecification],
                 validationFunctionUrl: String,
                 messageStyle: MessageStyle = AMFStyle): RdfModel =
    validator.shaclModel(validations, validationFunctionUrl, messageStyle)

  def apply(model: BaseUnit,
            profileName: ProfileName,
            messageStyle: MessageStyle = AMFStyle,
            env: Environment = Environment()): Future[AMFValidationReport] =
    validator.validate(model, profileName, messageStyle, env)

  def aggregateReport(model: BaseUnit,
                      profileName: ProfileName,
                      messageStyle: MessageStyle = AMFStyle): Future[AMFValidationReport] =
    validator.aggregateReport(model, profileName, messageStyle)

  def reset(): Unit = validator.reset()

  def nestedValidation[T](merger: ValidationsMerger)(k: => T): T = validator.nestedValidation(merger)(k)

  def disableValidations[T]()(f: () => T): T = validator.disableValidations()(f)

  def disableValidationsAsync[T]()(f: (() => Unit) => T): T = validator.disableValidationsAsync()(f)

  def reportConstraintFailure(level: String,
                              validationId: String,
                              targetNode: String,
                              targetProperty: Option[String] = None,
                              message: String = "",
                              position: Option[LexicalInformation] = None,
                              parserRun: Int,
                              location: Option[String]): Unit = {
    validator.reportConstraintFailure(
      level,
      validationId,
      targetNode,
      targetProperty,
      message,
      position,
      parserRun,
      location
    )
  }
}

class ValidationOptions() {
  val filterFields: Field => Boolean = (_: Field) => false
  var messageStyle: MessageStyle     = AMFStyle
  var level: String                  = "partial" // partial | full

  def toRenderOptions: RenderOptions = RenderOptions().withValidation.withFilterFieldsFunc(filterFields)

  def withMessageStyle(style: MessageStyle): ValidationOptions = {
    messageStyle = style
    this
  }

  def withFullValidation(): ValidationOptions = {
    level = "full"
    this
  }

  def withPartialValidation(): ValidationOptions = {
    level = "partial"
    this
  }

  def isPartialValidation(): Boolean = level == "partial"
}

object DefaultValidationOptions extends ValidationOptions {}
