package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.{ProfileName, ProfileNames, SeverityLevels, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.shapes.internal.validation.avro.{
  AvroSchemaReportValidationProcessor,
  BaseAvroSchemaPayloadValidator,
  ExampleUnknownException,
  InvalidAvroObject
}
import amf.shapes.internal.validation.common.ValidationProcessor
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.shapes.internal.validation.jsonschema.{InvalidJsonObject, InvalidJsonValue}

import scala.scalajs.js
import scala.scalajs.js.{JavaScriptException, SyntaxError}

class JsAvroShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration
) extends BaseAvroSchemaPayloadValidator(shape, mediaType, configuration) {

  override type LoadedObj    = String
  override type LoadedSchema = AvroSchema
  // TODO HERE YOU HAVE TO MAKE THE CALLS TO THE VALIDATOR JS LIBRARY. CHECK JsShapePayloadValidator FOR REFERENCE

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JsReportValidationProcessor(profileName, shape)

  protected def loadAvro(str: String): LoadedObj = {
//    val isObjectLike = str.startsWith("{") || str.startsWith("[")
//    try js.Dynamic.global.JSON.parse(str)
//    catch {
//      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] =>
//        if (isObjectLike) throw new InvalidJsonObject(e)
//        else throw new InvalidJsonValue(e)
//    }
    str
  }

  override protected def callValidator(
      schema: AvroSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    val validator = LazyAvro.default
    try {
      println("Llego a call validator the JSAvro")
//      val schema  = validator.parse(schema)
      val correct = schema.isValid(obj)

      val results: Seq[AMFValidationResult] =
        if (correct) Nil
        else {
          Seq(
            AMFValidationResult(
              message = "Validation failed: does not conform with Avro schema.",
              level = SeverityLevels.VIOLATION,
              targetNode = fragment.map(_.encodes.id).getOrElse(""),
              targetProperty = fragment.map(_.encodes.id),
              validationId = ExampleValidationErrorSpecification.id,
              position = fragment.flatMap(_.encodes.position()),
              location = fragment.flatMap(_.encodes.location()),
              source = schema
            )
          )
        }

      validationProcessor.processResults(results)
    } catch {
      case e: JavaScriptException =>
        validationProcessor.processException(e, fragment.map(_.encodes))
    }
  }

  override protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj] = {
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
    try {
      val schema = loadAvro(avroSchema.toString).asInstanceOf[AvroSchema]
      Right(Some(schema))
    } catch {
      case e: JavaScriptException =>
        val result = AMFValidationResult(
          message = s"Failed to load Avro schema: ${e.getMessage}",
          level = SeverityLevels.VIOLATION,
          targetNode = element.id,
          targetProperty = Some(element.id), // this is not correct should be the specific property of the element
          validationId = ExampleValidationErrorSpecification.id,
          position = element.position(),
          location = element.location(),
          source = e
        )
        validationProcessor.processResults(Seq(result))
        Left(AMFValidationReport(element.location().getOrElse(""), ProfileNames.AVROSCHEMA, Seq(result)))
    }
  }
  private case class JsReportValidationProcessor(
      val profileName: ProfileName,
      shape: Shape,
      protected var intermediateResults: Seq[AMFValidationResult] = Seq()
  ) extends AvroSchemaReportValidationProcessor {

    override def keepResults(r: Seq[AMFValidationResult]): Unit = intermediateResults ++= r

    override def processException(r: Throwable, element: Option[DomainElement]): AMFValidationReport = {
      val results = r match {
        case e: JavaScriptException =>
          Seq(
            AMFValidationResult(
              message = s"Internal error during Avro validation: ${e.getMessage}",
              level = SeverityLevels.VIOLATION,
              targetNode = element.map(_.id).getOrElse(""),
              targetProperty = None,
              validationId = ExampleValidationErrorSpecification.id,
              position = element.flatMap(_.position()),
              location = element.flatMap(_.location()),
              source = e
            )
          )
        case _ => processCommonException(r, element)
      }
      processResults(results)
    }
  }

  override protected def loadAvroSchema(text: String): AvroSchema = {
    println("entro")
    val  variable = LazyAvro.default.parse(text)
    println("salio")
    variable
  }
}
