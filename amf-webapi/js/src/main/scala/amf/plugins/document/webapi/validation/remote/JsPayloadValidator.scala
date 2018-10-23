package amf.plugins.document.webapi.validation.remote

import amf.ProfileName
import amf.core.model.document.PayloadFragment
import amf.core.validation.{AMFValidationResult, SeverityLevels}
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.plugins.domain.shapes.models.AnyShape

import scala.scalajs.js
import scala.scalajs.js.{JavaScriptException, SyntaxError}

class JsPayloadValidator(val shape: AnyShape) extends PlatformPayloadValidator(shape) {

  override type LoadedObj    = js.Dynamic
  override type LoadedSchema = js.Dictionary[js.Dynamic]

  private val env = Environment()

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JsReportValidationProcessor(profileName)

  override protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj] = {
    try {
      literalRepresentation(payload) map { payloadText =>
        loadJson(payloadText)
      }
    } catch {
      case _: ExampleUnknownException                                      => None
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] => throw new InvalidJsonObject(e)
    }
  }

  override protected def loadJson(str: String): LoadedObj = {
    js.Dynamic.global.JSON.parse(str)
  }

  override protected def loadSchema(jsonSchema: String): Option[LoadedSchema] = {
    var schemaNode = loadJson(jsonSchema).asInstanceOf[js.Dictionary[js.Dynamic]]
    schemaNode -= "x-amf-fragmentType"
    schemaNode -= "example"
    schemaNode -= "examples"
    Some(schemaNode)
  }

  override protected def callValidator(schema: LoadedSchema,
                                       obj: LoadedObj,
                                       fragment: Option[PayloadFragment],
                                       validationProcessor: ValidationProcessor): validationProcessor.Return = {

    val fast      = validationProcessor.isInstanceOf[BooleanValidationProcessor.type]
    val validator = if (fast) AjvValidator.fast() else AjvValidator()

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
              targetProperty = None,
              validationId = (Namespace.AmfParser + "example-validation-error").iri(),
              position = fragment.flatMap(_.encodes.position()),
              location = fragment.flatMap(_.encodes.location()),
              source = result
            )
          }
        } else Nil

        validationProcessor.processResults(results)
      }
    } catch {
      case e: scala.scalajs.js.JavaScriptException =>
        validationProcessor.processException(e, fragment)
    }
  }

  private def makeValidationMessage(validationResult: ValidationResult): String = {
    var pointer = validationResult.dataPath
    if (pointer.startsWith(".")) pointer = pointer.replaceFirst("\\.", "")
    (pointer + " " + validationResult.message).trim
  }
}

case class JsReportValidationProcessor(override val profileName: ProfileName) extends ReportValidationProcessor {

  override def processException(r: Throwable, fragment: Option[PayloadFragment]): Return = {
    val results = r match {
      case e: scala.scalajs.js.JavaScriptException =>
        Seq(
          AMFValidationResult(
            message = s"Internal error during validation ${e.getMessage}",
            level = SeverityLevels.VIOLATION,
            targetNode = fragment.map(_.encodes.id).getOrElse(""),
            targetProperty = None,
            validationId = (Namespace.AmfParser + "example-validation-error").iri(),
            position = fragment.flatMap(_.encodes.position()),
            location = fragment.flatMap(_.encodes.location()),
            source = e
          ))
      case other => processCommonException(other, fragment)
    }
    processResults(results)
  }
}

class JsParameterValidator(override val shape: AnyShape) extends JsPayloadValidator(shape) with ParameterValidator
