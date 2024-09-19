package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.ProfileNames.AVROSCHEMA
import amf.core.client.common.validation.{ProfileName, ProfileNames, SeverityLevels, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.internal.validation.avro.{AvroRawNotFound, AvroSchemaReportValidationProcessor, BaseAvroSchemaPayloadValidator, InvalidAvroSchema}
import amf.shapes.internal.validation.common.ValidationProcessor
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.SchemaException
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

  override protected type LoadedObj    = SchemaShape // The Raw is the original payload to validate
  override protected type LoadedSchema = Schema

  override protected def loadAvro(text: String): LoadedObj = SchemaShape(shape.annotations).withRaw(text)

  override protected def loadAvroSchema(text: String): LoadedSchema = try {
    parser.parse(text)
  } catch {
    case e: Exception =>
      throw new InvalidAvroSchema(e)
  }

  override def validateAvroSchema(): Seq[AMFValidationResult] = try {
    val raw = getAvroRaw(shape) match {
      case Some(raw) => raw
      case None      => throw new AvroRawNotFound()
    }
    parser.parse(raw)
    Nil
  } catch {
    case e: Exception => getReportProcessor(AVROSCHEMA).processException(e, Some(shape)).results
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
      avroSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]] = {
    try {
      val schema = loadAvroSchema(avroSchema.toString)
      Right(Some(schema))
    } catch {
      case e: Exception =>
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
            validationId = SchemaException.id,
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
            validationId = SchemaException.id,
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
            validationId = SchemaException.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = invalidType
          )
        )

      case numberFormatException: NumberFormatException =>
        Seq(
          AMFValidationResult(
            message = s"default value should be a number: ${numberFormatException.getMessage}",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = None,
            validationId = SchemaException.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = numberFormatException
          )
        )

      case other =>
        super.processCommonException(other, element)
    }
    processResults(results)
  }
}
