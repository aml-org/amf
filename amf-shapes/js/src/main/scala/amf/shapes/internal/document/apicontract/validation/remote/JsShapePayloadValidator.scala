package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.{ProfileName, SeverityLevels, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.shapes.internal.validation.jsonschema._

import java.io.{BufferedReader, InputStream, InputStreamReader}
import scala.scalajs.js
import scala.scalajs.js.{Dictionary, JavaScriptException, SyntaxError}

class JsShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration,
    private val shouldFailFast: Boolean = false
) extends BaseJsonSchemaPayloadValidator(shape, mediaType, configuration, shouldFailFast) {

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
    val isObjectLike = str.startsWith("{") || str.startsWith("[")
    try js.Dynamic.global.JSON.parse(str)
    catch {
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] =>
        if (isObjectLike) throw new InvalidJsonObject(e)
        else throw new InvalidJsonValue(e)
    }
  }

  override protected def loadJson(stream: InputStream): js.Dynamic = {
    try {
      js.Dynamic.global.JSON.parse(streamToString(stream))
    } catch {
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] => throw new JsonSyntaxError(e)
    }
  }

  private def streamToString(is: InputStream) = {
    val rd: BufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
    val builder            = new StringBuilder()
    try {
      var line = rd.readLine
      while (line != null) {
        builder.append(line + "\n")
        line = rd.readLine
      }
    } finally {
      rd.close()
    }
    builder.toString
  }

  override protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[Dictionary[js.Dynamic]]] = {
    var schemaNode = loadJson(jsonSchema.toString).asInstanceOf[Dictionary[js.Dynamic]]
    schemaNode -= "x-amf-fragmentType"
    schemaNode -= "example"
    schemaNode -= "examples"
    schemaNode -= "x-amf-examples"
    Right(Some(schemaNode))
  }

  override protected def callValidator(
      schema: Dictionary[js.Dynamic],
      obj: js.Dynamic,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {

    val validator = if (shouldFailFast) LazyAjv.fast else LazyAjv.default
    try {
      val correct = validator.validate(schema.asInstanceOf[js.Object], obj)

      val results: Seq[AMFValidationResult] =
        if (correct) Nil
        else {
          validator.errors.getOrElse(js.Array[ValidationResult]()).map { result =>
            asAmfResult(result, fragment)
          }
        }
      validationProcessor.processResults(results)
    } catch {
      case e: JavaScriptException =>
        validationProcessor.processException(e, fragment.map(_.encodes))
    }
  }

  override protected def callValidatorForStream(
      schema: Dictionary[js.Dynamic],
      stream: InputStream,
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {

    val validator = LazyAjv.fast

    try {
      val payload = loadJson(stream)
      val correct = validator.validate(schema.asInstanceOf[js.Object], payload)

      val results: Seq[AMFValidationResult] =
        if (correct) Nil
        else {
          validator.errors.getOrElse(js.Array[ValidationResult]()).map { result =>
            asAmfResult(result, None)
          }
        }

      validationProcessor.processResults(results)
    } catch {
      case e: Throwable => validationProcessor.processException(e, None)
    }
  }

  private def asAmfResult(result: ValidationResult, fragment: Option[PayloadFragment]) = {
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

  private def makeValidationMessage(validationResult: ValidationResult): String = {
    var pointer = validationResult.dataPath
    if (pointer.startsWith(".")) pointer = pointer.replaceFirst("\\.", "")
    (pointer + " " + validationResult.message).trim
  }

}

case class JsReportValidationProcessor(
    override val profileName: ProfileName,
    override protected var intermediateResults: Seq[AMFValidationResult] = Seq()
) extends ReportValidationProcessor {

  override def keepResults(r: Seq[AMFValidationResult]): Unit = intermediateResults ++= r

  override def processException(r: Throwable, element: Option[DomainElement]): AMFValidationReport = {
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
          )
        )
      case other => processCommonException(other, element)
    }
    processResults(results)
  }
}
