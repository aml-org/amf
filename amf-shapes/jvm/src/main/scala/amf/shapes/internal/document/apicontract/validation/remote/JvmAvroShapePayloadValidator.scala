package amf.shapes.internal.document.apicontract.validation.remote

import amf.core.client.common.validation.{ProfileName, ValidationMode}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.client.scala.validation.payload.ShapeValidationConfiguration
import amf.shapes.internal.validation.avro.{AvroSchemaReportValidationProcessor, BaseAvroSchemaPayloadValidator}
import amf.shapes.internal.validation.common.ValidationProcessor

class JvmAvroShapePayloadValidator(
    private val shape: Shape,
    private val mediaType: String,
    protected val validationMode: ValidationMode,
    private val configuration: ShapeValidationConfiguration,
    private val shouldFailFast: Boolean = false
) extends BaseAvroSchemaPayloadValidator(shape, mediaType, configuration, shouldFailFast) {

  // TODO HERE YOU HAVE TO MAKE THE CALLS TO THE VALIDATOR JVM LIBRARY. CHECK JvmShapePayloadValidator FOR REFERENCE

  override protected def getReportProcessor(profileName: ProfileName): ValidationProcessor = ???

  override protected type LoadedObj    = this.type
  override protected type LoadedSchema = this.type

  override protected def loadAvro(text: String): LoadedObj = ???

  override protected def callValidator(
      schema: LoadedSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = ???

  override protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedSchema] =
    ???

  override protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]] = ???
}

case class JvmReportValidationProcessor(
    override val profileName: ProfileName,
    shape: Shape,
    override protected var intermediateResults: Seq[AMFValidationResult] = Seq()
) extends AvroSchemaReportValidationProcessor {

  override def processException(r: Throwable, element: Option[DomainElement]): AMFValidationReport = ???

  override def keepResults(r: Seq[AMFValidationResult]): Unit = ???
}
