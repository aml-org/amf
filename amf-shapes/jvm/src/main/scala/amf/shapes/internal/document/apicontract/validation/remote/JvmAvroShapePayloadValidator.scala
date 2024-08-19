package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.{ProfileName, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.shapes.internal.validation.avro.{AvroSchemaReportValidationProcessor, BaseAvroSchemaPayloadValidator}
import amf.shapes.internal.validation.common.ValidationProcessor
import amf.shapes.internal.validation.jsonschema.InvalidJsonValue
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericRecord}
import org.apache.avro.io.DecoderFactory

class JvmAvroShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration,
    private val shouldFailFast: Boolean = false
) extends BaseAvroSchemaPayloadValidator(shape, mediaType, configuration, shouldFailFast) {
  lazy val parser = new Schema.Parser()
  // TODO HERE YOU HAVE TO MAKE THE CALLS TO THE VALIDATOR JVM LIBRARY. CHECK JvmShapePayloadValidator FOR REFERENCE

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor =
    JvmReportValidationProcessor(profileName, shape)

//  override protected type LoadedObj    = Object // todo: shouldn't it be org.apache.avro.JsonProperties?
  override protected type LoadedObj    = String
  override protected type LoadedSchema = Schema

  override protected def loadAvro(text: String): LoadedObj = try {
//    val schema: Schema = parser.parse(text)
//    schema // todo: shouldn't load a Payload instead of the schema?
    text
  } catch {
    // todo: catch specific validations thrown by the library like withJsonExceptionCatching() in JvmShapePayloadValidator
    case e: RuntimeException => throw new InvalidJsonValue(e)
  }

  override protected def loadAvroSchema(text: String): LoadedSchema = try {
    val schema: Schema = parser.parse(text)
    schema
  } catch {
    // todo: catch specific validations thrown by the library like withJsonExceptionCatching() in JvmShapePayloadValidator
    case e: RuntimeException => throw new InvalidJsonValue(e)
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
      val decoder = DecoderFactory.get.jsonDecoder(schema, obj)
      // Attempt to read the JSON into a GenericRecord
      reader.read(null, decoder)
      validationProcessor.processResults(Nil)
    } catch {
      case exception: Throwable =>
        validationProcessor.processException(exception, fragment.map(_.encodes))
    }
  }

  override protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj] =
    ??? // todo: WTF IS THIS

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

  override def processException(r: Throwable, element: Option[DomainElement]): AMFValidationReport = {
    val results = r match {
      // todo: catch and process each specific exception like the JvmJsonSchemaReportValidationProcessor does in json-schema
      case other =>
        super.processCommonException(other, element)
    }
    processResults(results)
  }
}
