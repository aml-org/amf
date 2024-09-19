package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.ProfileNames.AVROSCHEMA
import amf.core.client.common.validation.{ProfileName, ProfileNames, SeverityLevels, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.internal.validation.avro.{
  AvroRawNotFound,
  AvroSchemaReportValidationProcessor,
  BaseAvroSchemaPayloadValidator
}
import amf.shapes.internal.validation.common.ValidationProcessor
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.{
  ExampleValidationErrorSpecification,
  SchemaException
}

import scala.scalajs.js.JavaScriptException

class JsPayloadValidationError(message: String) extends RuntimeException(message)

class JsAvroShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration
) extends BaseAvroSchemaPayloadValidator(shape, mediaType, configuration) {

  override type LoadedObj    = SchemaShape
  override type LoadedSchema = AvroType

  private lazy val avroJs = LazyAvroJs.default

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JsReportValidationProcessor(profileName, shape)

  // This loads the payload
  protected def loadAvro(text: String): LoadedObj = SchemaShape(shape.annotations).withRaw(text)

  // This loads the Shape effectively
  override protected def loadAvroSchema(text: String): LoadedSchema =
    avroJs.parse(AvroJsValidator.parseJsonObject(text))

  // This loads the Shape
  override protected def loadSchema(
      avroSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]] = {
    try {
      val schema = loadAvroSchema(avroSchema.toString)
      Right(Some(schema))
    } catch {
      case e: JavaScriptException =>
        val result = AMFValidationResult(
          message = s"Error in AVRO Schema: ${e.getMessage}",
          level = SeverityLevels.VIOLATION,
          targetNode = Option(element.id).getOrElse(""),
          targetProperty = Option(element.id), // this is not correct should be the specific property of the element
          validationId = SchemaException.id,
          position = element.position(),
          location = element.location(),
          source = e
        )
        validationProcessor.processResults(Seq(result))
        Left(AMFValidationReport(element.location().getOrElse(""), ProfileNames.AVROSCHEMA, Seq(result)))
    }
  }

  override protected def callValidator(
      schema: LoadedSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    try {
      val collector = new ErrorListener()
      val payload   = obj.raw.value()
      schema.isValid(AvroJsValidator.parseJson(payload), collector)
      validationProcessor.processResults(Nil)
    } catch {
      case e: JsPayloadValidationError =>
        val result = AMFValidationResult(
          message = e.getMessage,
          level = SeverityLevels.VIOLATION,
          targetNode = fragment.map(_.encodes.id).getOrElse(""),
          targetProperty = fragment.map(_.encodes.id),
          validationId = ExampleValidationErrorSpecification.id,
          position = fragment.flatMap(_.encodes.position()),
          location = fragment.flatMap(_.encodes.location()),
          source = schema
        )
        validationProcessor.processResults(Seq(result))
      case e: JavaScriptException =>
        validationProcessor.processException(e, fragment.map(_.encodes))
    }
  }

  private case class JsReportValidationProcessor(
      override val profileName: ProfileName,
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

  override def validateAvroSchema(): Seq[AMFValidationResult] = try {
    val raw = getAvroRaw(shape) match {
      case Some(raw) => raw
      case None      => throw new AvroRawNotFound()
    }
    avroJs.parse(AvroJsValidator.parseJsonObject(raw))
    Nil
  } catch {
    case e: Exception => getReportProcessor(AVROSCHEMA).processException(e, Some(shape)).results
  }
}
