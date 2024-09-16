package amf.shapes.internal.validation.avro

import amf.core.client.common.validation._
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain._
import amf.core.client.scala.validation.payload.{PayloadParsingResult, ShapeValidationConfiguration}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.internal.remote.Mimes._
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.AVRORawSchema
import amf.shapes.internal.validation.common.{
  CommonBaseSchemaPayloadValidator,
  ReportValidationProcessor,
  ValidationProcessor
}
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification

import scala.concurrent.{ExecutionContext, Future}

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class InvalidAvroObject(e: Throwable)       extends RuntimeException(e)
class InvalidAvroValue(e: Throwable)        extends RuntimeException(e)

object BaseAvroSchemaPayloadValidator {
  val supportedMediaTypes: Seq[String] = Seq(`application/json`)
}

abstract class BaseAvroSchemaPayloadValidator(
    shape: Shape,
    mediaType: String,
    configuration: ShapeValidationConfiguration
) extends CommonBaseSchemaPayloadValidator {

  protected implicit val executionContext: ExecutionContext = configuration.executionContext

  override protected val supportedMediaTypes: Seq[String] = BaseAvroSchemaPayloadValidator.supportedMediaTypes

  override def validate(payload: String): Future[AMFValidationReport] = {
    Future.successful(validateForPayload(payload, getReportProcessor))
  }

  override def validate(fragment: PayloadFragment): Future[AMFValidationReport] = {
    Future.successful(validateForFragment(fragment, getReportProcessor))
  }

  override def syncValidate(payload: String): AMFValidationReport = {
    validateForPayload(payload, getReportProcessor)
  }

  protected def loadAvro(text: String): LoadedObj

  protected def loadAvroSchema(text: String): LoadedSchema

  def validateAvroSchema(): Seq[AMFValidationResult]

  def getAvroRaw(shape: Shape): Option[AVRORawSchema] = shape.annotations.find(classOf[AVRORawSchema])

  protected def validateForFragment(
      fragment: PayloadFragment,
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    try {
      performValidation(buildCandidate(fragment), validationProcessor)
    } catch {
      case e: InvalidAvroObject => validationProcessor.processException(e, Some(fragment.encodes))
      case e: InvalidAvroValue  => validationProcessor.processException(e, Some(fragment.encodes))
    }
  }

  protected def validateForPayload(payload: String, validationProcessor: ValidationProcessor): AMFValidationReport = {
    if (!isValidMediaType(mediaType)) {
      validationProcessor.processResults(Seq(mediaTypeError(mediaType)))
    } else
      try {
        performValidation(buildCandidate(mediaType, payload), validationProcessor)
      } catch {
        case e: InvalidAvroObject => validationProcessor.processException(e, None)
        case e: InvalidAvroValue  => validationProcessor.processException(e, None)
      }
  }

  private def generateSchema(
      fragmentShape: Shape,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]] = {
    val schemaOption: Option[CharSequence] = getRawAvroSchema(fragmentShape, validationProcessor)
    schemaOption match {
      case Some(charSequence) => loadSchema(charSequence, fragmentShape, validationProcessor)
      case None               => Right(None)
    }
  }

  private def getRawAvroSchema(shape: Shape, validationProcessor: ValidationProcessor): Option[CharSequence] = {
    getAvroRaw(shape) match {
      case Some(ann) => Some(ann.avroRawSchema)
      case None =>
        val result = AMFValidationResult(
          "should have AVRO Raw annotation",
          SeverityLevels.VIOLATION,
          shape.id,
          Some(shape.id),
          "",
          shape.position(),
          shape.location(),
          shape
        )
        validationProcessor.keepResults(Seq(result))
        None
    }
  }

  private def getOrCreateSchema(
      s: AnyShape,
      validationProcessor: ValidationProcessor
  ): Either[AMFValidationReport, Option[LoadedSchema]] = {
    schemas.get(s.id) match {
      case Some(schema) => Right(Some(schema))
      case _ =>
        generateSchema(s, validationProcessor) match {
          case Right(maybeSchema) =>
            maybeSchema.foreach { schemas.put(s.id, _) }
            Right(maybeSchema)
          case Left(result) =>
            Left(result)
        }
    }
  }

  protected def buildPayloadObj(mediaType: String, payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) =
    if (mediaType == `application/json`)
      (Some(loadAvro(payload)), None)
    else (None, None)

  private def performValidation(
      payload: (Option[LoadedObj], Option[PayloadParsingResult]),
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    payload match {
      case (_, Some(result)) if result.hasError => validationProcessor.processResults(result.results)
      case (Some(obj), resultOption) =>
        val fragmentOption = resultOption.map(_.fragment)
        try {
          {
            resultOption match {
              case _ if shape.isInstanceOf[AnyShape] =>
                getOrCreateSchema(shape.asInstanceOf[AnyShape], validationProcessor)
              case _ =>
                Left(
                  validationProcessor.processResults(
                    Seq(
                      AMFValidationResult(
                        "Cannot validate shape that is not an any shape",
                        defaultSeverity,
                        "",
                        Some(shape.id),
                        ExampleValidationErrorSpecification.id,
                        shape.position(),
                        shape.location(),
                        null
                      )
                    )
                  )
                )
            }
          } match {
            case Right(Some(schema)) => // Schema obtained successfully, calling validator with it
              callValidator(schema, obj, fragmentOption, validationProcessor)
            case Left(result) => // Error occurred during schema generation, returning that result
              result
            case _ => // No schema or payload error, returning empty results
              validationProcessor.processResults(Nil)
          }
        } catch {
          case e: Exception =>
            println(s"Exception caught during validation: ${e.getMessage}")
            validationProcessor.processException(e, fragmentOption.map(_.encodes))
        }

      case _ =>
        println("No valid payload or results, returning empty results")
        validationProcessor.processResults(Nil) // ignore
    }
  }

  private def buildCandidate(mediaType: String, payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) =
    buildPayloadObj(mediaType, payload)

  private def buildCandidate(payload: PayloadFragment): (Option[LoadedObj], Option[PayloadParsingResult]) =
    (loadDataNodeString(payload), Some(PayloadParsingResult(payload, Nil)))

}

trait AvroSchemaReportValidationProcessor extends ReportValidationProcessor {
  override def processCommonException(r: Throwable, element: Option[DomainElement]): Seq[AMFValidationResult] = {
    r match {
      case other =>
        Seq(
          AMFValidationResult(
            message = s"Exception thrown in validation: ${r.getMessage}",
            level = SeverityLevels.VIOLATION,
            targetNode = element.map(_.id).getOrElse(""),
            targetProperty = None,
            validationId = ExampleValidationErrorSpecification.id,
            position = element.flatMap(_.position()),
            location = element.flatMap(_.location()),
            source = other
          )
        )
    }
  }
}
