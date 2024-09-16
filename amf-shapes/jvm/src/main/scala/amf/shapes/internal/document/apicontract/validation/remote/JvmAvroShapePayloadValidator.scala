package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.ProfileNames.AVROSCHEMA
import amf.core.client.common.validation.{ProfileName, SeverityLevels, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.internal.validation.avro.{AvroSchemaReportValidationProcessor, BaseAvroSchemaPayloadValidator}
import amf.shapes.internal.validation.common.ValidationProcessor
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.shapes.internal.validation.jsonschema.{InvalidAvroSchema, InvalidJsonValue}
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.DecoderFactory
import org.apache.avro.{AvroTypeException, Schema, SchemaParseException}

class JvmAvroShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration
) extends BaseAvroSchemaPayloadValidator(shape, mediaType, configuration) {
  lazy val parser = new Schema.Parser()

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JvmReportValidationProcessor(profileName, shape)

  override protected type LoadedObj    = SchemaShape
  override protected type LoadedSchema = Schema

  override protected def loadAvro(text: String): LoadedObj =
    SchemaShape(shape.annotations).withRaw(text)

  override protected def loadAvroSchema(text: String): LoadedSchema = try {
    val schema: Schema = parser.parse(text)
    schema
  } catch {
    case e: RuntimeException => throw new InvalidJsonValue(e)
  }

  override def validateAvroSchema(): Seq[AMFValidationResult] = try {
    val raw = getAvroRaw(shape) match {
      case Some(rawAnn) => rawAnn.avroRawSchema
      case None         => throw new InvalidAvroSchema(new RuntimeException())
    }
    parser.parse(raw)
    Nil
  } catch {
    case e: RuntimeException => getReportProcessor(AVROSCHEMA).processException(e, Some(shape)).results
  }

  override protected def callValidator(
      schema: LoadedSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    try {
      // Create a DatumReader for GenericRecord
      val reader = new GenericDatumReader[GenericRecord](schema)
      // Create a Decoder from the JSON string
      val decoder = DecoderFactory.get.jsonDecoder(schema, obj.raw.value())
      // Attempt to read the JSON into a GenericRecord
      reader.read(null, decoder)
      validationProcessor.processResults(Nil)
    } catch {
      case exception: Throwable =>
        validationProcessor.processException(exception, Some(obj))
    }
  }

  // we don't need to JSON-parse the raw as in JSON Schema because the validation plugin uses the raw string
  override protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj] =
    literalRepresentation(payload) map { raw =>
      SchemaShape(payload.annotations).withRaw(raw).withMediaType(payload.mediaType.value())
    }

  override protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]] = {
    try {
      val schema = loadAvroSchema(jsonSchema.toString)
      Right(Some(schema))
    } catch {
      case e: Exception => Left(validationProcessor.processException(e, Some(element)))
    }
  }
}

case class JvmReportValidationProcessor(
    override val profileName: ProfileName,
    shape: Shape,
    override protected var intermediateResults: Seq[AMFValidationResult] = Seq()
) extends AvroSchemaReportValidationProcessor {

  override def keepResults(r: Seq[AMFValidationResult]): Unit = intermediateResults ++= r

  // todo: catch and process each specific exception
  override def processException(r: Throwable, element: Option[DomainElement]): AMFValidationReport = {
    val results = r match {
      case e: AvroTypeException =>
        Seq(
          AMFValidationResult(
            message = s"invalid schema type: ${e.getMessage}",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = Some(e.getMessage.split(":").last.trim),
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = e
          )
        )

      case e: SchemaParseException =>
        Seq(
          AMFValidationResult(
            message = e.getMessage,
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = Some(e.getMessage.split(":").last.trim),
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = e
          )
        )
// org.apache.avro.compiler.UnresolvedSchema
      case invalidType: NullPointerException if invalidType.getMessage.contains("") =>
        Seq(
          AMFValidationResult(
            message = s"Invalid type: ${invalidType.getMessage.split("_").head}",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = Some(invalidType.getMessage.split(":").last.trim),
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = invalidType
          )
        )

      case other =>
        super.processCommonException(other, element)
    }
    processResults(results)
  }
}
