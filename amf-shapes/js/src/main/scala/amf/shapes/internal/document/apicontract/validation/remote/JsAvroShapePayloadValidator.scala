package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.{AvroSchemaProfile, ProfileName, SeverityLevels, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.shapes.internal.validation.avro.{BaseAvroSchemaPayloadValidator, ExampleUnknownException, InvalidAvroObject}
import amf.shapes.internal.validation.common.ValidationProcessor
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.shapes.internal.validation.jsonschema.{InvalidJsonObject, InvalidJsonValue}

import scala.scalajs.js
import scala.scalajs.js.{Dictionary, JavaScriptException, SyntaxError}

class JsAvroShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration,
    private val shouldFailFast: Boolean = false
) extends BaseAvroSchemaPayloadValidator(shape, mediaType, configuration, shouldFailFast) {

  override type LoadedObj    = js.Dynamic
  override type LoadedSchema = AvroSchema
  // TODO HERE YOU HAVE TO MAKE THE CALLS TO THE VALIDATOR JS LIBRARY. CHECK JsShapePayloadValidator FOR REFERENCE

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JsReportValidationProcessor(profileName)
  override protected def loadAvro(str: String): LoadedObj = {
    val isObjectLike = str.startsWith("{") || str.startsWith("[")
    try js.Dynamic.global.JSON.parse(str)
    catch {
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] =>
        if (isObjectLike) throw new InvalidJsonObject(e)
        else throw new InvalidJsonValue(e)
    }
  }

  override protected def callValidator(
      schema: LoadedSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    val validator = LazyAvro.default
    try {
      val schema  = validator.parse(schema)
      val correct = schema.isValid(obj)

      val results: Seq[AMFValidationResult] =
        if (correct) Nil
        else {
          Nil
          // todo: get errors from the callback of the isValid method in the validator
          // (and process them using asAmfResult)
        }

      validationProcessor.processResults(results)
    } catch {
      case e: JavaScriptException =>
        validationProcessor.processException(e, fragment.map(_.encodes))
    }
  }

  override protected def loadDataNodeString(payload: PayloadFragment): Option[AvroSchema] = {
    try {
      literalRepresentation(payload) map { payloadText =>
        loadAvro(payloadText)
      }
    } catch {
      case _: ExampleUnknownException                                      => None
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] => throw new InvalidAvroObject(e)
    }
  }

  override protected def loadSchema(
      avroSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[AvroSchema]] = {
    val schema = loadAvro(avroSchema.toString)
    Right(Some(schema))
  }
}
