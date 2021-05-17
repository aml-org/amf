package amf.plugins.document.webapi.validation.remote
import amf.ProfileName
import amf.client.plugins.ValidationMode
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.validations.ShapePayloadValidations.ExampleValidationErrorSpecification

import scala.scalajs.js
import scala.scalajs.js.{Dictionary, JavaScriptException, SyntaxError}

class JsPayloadValidator(val shape: Shape, val validationMode: ValidationMode, val configuration: ValidationConfiguration)
    extends PlatformPayloadValidator(shape, configuration) {

  override type LoadedObj    = js.Dynamic
  override type LoadedSchema = Dictionary[js.Dynamic]

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JsReportValidationProcessor(profileName)

  override protected def loadDataNodeString(payload: PayloadFragment): Option[js.Dynamic] = {
    try {
      literalRepresentation(payload) map { payloadText =>
        loadJson(payloadText)
      }
    } catch {
      case _: ExampleUnknownException                                      => None
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] => throw new InvalidJsonObject(e)
    }
  }

  override protected def loadJson(str: String): js.Dynamic = {
    try js.Dynamic.global.JSON.parse(str)
    catch {
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] => throw new InvalidJsonObject(e)
    }
  }

  override protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor): Either[validationProcessor.Return, Option[Dictionary[js.Dynamic]]] = {
    var schemaNode = loadJson(jsonSchema.toString).asInstanceOf[Dictionary[js.Dynamic]]
    schemaNode -= "x-amf-fragmentType"
    schemaNode -= "example"
    schemaNode -= "examples"
    schemaNode -= "x-amf-examples"
    Right(Some(schemaNode))
  }

  override protected def callValidator(schema: Dictionary[js.Dynamic],
                                       obj: js.Dynamic,
                                       fragment: Option[PayloadFragment],
                                       validationProcessor: ValidationProcessor): validationProcessor.Return = {

    val fast      = validationProcessor.isInstanceOf[BooleanValidationProcessor.type]
    val validator = if (fast) LazyAjv.fast else LazyAjv.default

    try {
      val correct = validator.validate(schema.asInstanceOf[js.Object], obj)

      if (fast) correct.asInstanceOf[validationProcessor.Return]
      else {
        val results: Seq[AMFValidationResult] = if (!correct) {
          validator.errors.getOrElse(js.Array[ValidationResult]()).map { result =>
            AMFValidationResult(
              message = makeValidationMessage(result),
              level = SeverityLevels.VIOLATION,
              targetNode = fragment.map(_.encodes.id).getOrElse(""),
              targetProperty = fragment.map(_.encodes.id),
              validationId = ExampleValidationErrorSpecification.id,
              position = fragment.flatMap(_.encodes.position()),
              location = fragment.flatMap(_.encodes.location()),
              source = result
            )
          }
        } else Nil

        validationProcessor.processResults(results)
      }
    } catch {
      case e: JavaScriptException =>
        validationProcessor.processException(e, fragment.map(_.encodes))
    }
  }

  private def makeValidationMessage(validationResult: ValidationResult): String = {
    var pointer = validationResult.dataPath
    if (pointer.startsWith(".")) pointer = pointer.replaceFirst("\\.", "")
    (pointer + " " + validationResult.message).trim
  }

}

case class JsReportValidationProcessor(override val profileName: ProfileName,
                                       override protected var intermediateResults: Seq[AMFValidationResult] = Seq())
    extends ReportValidationProcessor {

  override def keepResults(r: Seq[AMFValidationResult]): Unit = intermediateResults ++= r

  override def processException(r: Throwable, element: Option[DomainElement]): Return = {
    val results = r match {
      case e: scala.scalajs.js.JavaScriptException =>
        Seq(
          AMFValidationResult(
            message = s"Internal error during validation ${e.getMessage}",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = element.map(_.id),
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = e
          ))
      case other => processCommonException(other, element)
    }
    processResults(results)
  }
}
