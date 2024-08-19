package amf.shapes.internal.validation.avro

import amf.core.client.common.validation._
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.parse.document.{ErrorHandlingContext, ParsedReference, SyamlParsedDocument}
import amf.core.client.scala.validation.payload.{PayloadParsingResult, ShapeValidationConfiguration}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.internal.datanode.{DataNodeParser, DataNodeParserContext}
import amf.core.internal.parser.domain.{FragmentRef, JsonParserFactory, SearchScope}
import amf.core.internal.plugins.syntax.{SYamlAMFParserErrorHandler, SyamlSyntaxRenderPlugin}
import amf.core.internal.remote.Mimes._
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.AVRORawSchema
import amf.shapes.internal.spec.common.emitter.PayloadEmitter
import amf.shapes.internal.validation.common.{
  CommonBaseSchemaPayloadValidator,
  ReportValidationProcessor,
  ValidationProcessor
}
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.shapes.internal.validation.jsonschema.ScalarPayloadForParam
import org.mulesoft.common.client.lexical.SourceLocation
import org.yaml.model.{IllegalTypeHandler, YError}
import org.yaml.parser.YamlParser

import java.io.StringWriter
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
    configuration: ShapeValidationConfiguration,
    shouldFailFast: Boolean
) extends CommonBaseSchemaPayloadValidator {

  protected implicit val executionContext: ExecutionContext = configuration.executionContext

  override protected val supportedMediaTypes: Seq[String] = BaseAvroSchemaPayloadValidator.supportedMediaTypes

  override def validate(payload: String): Future[AMFValidationReport] = {
    println("llego al baseAvroSchema")
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

  /* i need to do this check?? */
  protected def validateForPayload(
      payload: String,
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    println(payload)
    if (!isValidMediaType(mediaType)) {
      validationProcessor.processResults(
        Seq(mediaTypeError(mediaType))
      )
    } else
      println("entro al else the validateForPayload")
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

  protected def literalRepresentation(payload: PayloadFragment): Option[String] = {
    val futureText = payload.raw match {
      case Some("") => None
      case _ =>
        val document = PayloadEmitter(payload.encodes)(UnhandledErrorHandler).emitDocument()
        val writer   = new StringWriter()
        SyamlSyntaxRenderPlugin.emit(`application/json`, SyamlParsedDocument(document), writer).map(_.toString)
    }

    futureText map { text =>
      payload.encodes match {
        case node: ScalarNode
            if node.dataType
              .option()
              .contains(DataType.String) && text.nonEmpty && text.head != '"' =>
          "\"" + text.stripLineEnd + "\""
        case _ => text.stripLineEnd
      }
    }
  }

  private def getRawAvroSchema(shape: Shape, validationProcessor: ValidationProcessor): Option[CharSequence] = {
    val raw = shape.annotations.find(classOf[AVRORawSchema]).map(_.value)
    if (raw.isEmpty) {
      // TODO complete the result to make it a valid violation that the shape doesn't have the raw annotation
      // emitirlo?
      val result = AMFValidationResult(
        "should have AVRO Raw annotation",
        SeverityLevels.VIOLATION,
        shape.id,
        None,
        "",
        None,
        None,
        shape
      )
      validationProcessor.keepResults(Seq(result))
    }
    raw
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

  protected def buildPayloadObj(
      mediaType: String,
      payload: String
  ): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    if (mediaType == `application/json` && validationMode != ScalarRelaxedValidationMode)
      (Some(loadAvro(payload)), None)
    else {
      buildPayloadNode(mediaType, payload)
    }
  }

  private def parsePayloadWithErrorHandler(payload: String, mediaType: String): PayloadParsingResult = {
    val errorHandler = configuration.eh()
    PayloadParsingResult(parsePayload(payload, mediaType, errorHandler), errorHandler.getResults)
  }

  private def parsePayload(payload: String, mediaType: String, errorHandler: AMFErrorHandler): PayloadFragment = {
    val options = generateParsingOptions()
    val ctx     = dataNodeParsingCtx(errorHandler, options.getMaxYamlReferences, options.getMaxJsonYamlDepth)

    val parser = mediaType match {
      case `application/json` => JsonParserFactory.fromChars(payload, options.getMaxJsonYamlDepth)(errorHandler)
      case _ =>
        YamlParser(payload, options.getMaxJsonYamlDepth)(
          new SYamlAMFParserErrorHandler(errorHandler)
        ) // todo should fail?
    }
    val node = parser.document().node
    val parsedNode =
      if (node.isNull) ScalarNode(payload, None).withDataType(DataType.Nil)
      else DataNodeParser(node)(ctx).parse()
    PayloadFragment(parsedNode, mediaType)
  }

  private def generateParsingOptions(): ParsingOptions = {
    var po = ParsingOptions()
    po = configuration.maxYamlReferences.foldLeft(po) { (options, myr) =>
      options.setMaxYamlReferences(myr)
    }
    po = configuration.maxJsonYamlDepth.foldLeft(po) { (options, md) =>
      options.setMaxJsonYamlDepth(md)
    }
    po
  }

  private def dataNodeParsingCtx(
      errorHandler: AMFErrorHandler,
      maxYamlRefs: Option[Int],
      maxYamlJsonDepth: Option[Int]
  ): ErrorHandlingContext with DataNodeParserContext with IllegalTypeHandler = {
    new ErrorHandlingContext with DataNodeParserContext with IllegalTypeHandler {

      override implicit val eh: AMFErrorHandler = errorHandler
      val syamleh                               = new SYamlAMFParserErrorHandler(errorHandler)
      override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
        eh.violation(violationId, node, message, "")
      override def violation(violationId: ValidationSpecification, node: AmfObject, message: String): Unit =
        eh.violation(violationId, node, message, "")
      override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] = None
      override def refs: Seq[ParsedReference]                                                          = Seq.empty
      override def getMaxYamlReferences: Option[Int]                                                   = maxYamlRefs
      override def getMaxYamlJsonDepth: Option[Int]    = maxYamlJsonDepth
      override def fragments: Map[String, FragmentRef] = Map.empty

      override def handle[T](error: YError, defaultValue: T): T = syamleh.handle(error, defaultValue)

      override def violation(
          specification: ValidationSpecification,
          node: String,
          message: String,
          loc: SourceLocation
      ): Unit = eh.violation(specification, node, message, loc)
    }
  }

  protected def buildPayloadNode(
      mediaType: String,
      payload: String
  ): (Option[LoadedObj], Some[PayloadParsingResult]) = {
    val fixedResult = parsePayloadWithErrorHandler(payload, mediaType) match {
      case result if !result.hasError && validationMode == ScalarRelaxedValidationMode =>
        val frag = ScalarPayloadForParam(result.fragment, shape)
        result.copy(fragment = frag)
      case other => other
    }
    if (!fixedResult.hasError) (loadDataNodeString(fixedResult.fragment), Some(fixedResult))
    else (None, Some(fixedResult))
  }

  private def performValidation(
      payload: (Option[LoadedObj], Option[PayloadParsingResult]),
      validationProcessor: ValidationProcessor
  ): AMFValidationReport = {
    println("entro al performValidation del baseAvro")
    payload match {
      case (_, Some(result)) if result.hasError =>  validationProcessor.processResults(result.results)
      case (Some(obj), resultOption) =>
        println("entro al segundo case de perfomValidation")
        val fragmentOption = resultOption.map(_.fragment)
        try {
          {
            resultOption match {
              case _ if shape.isInstanceOf[AnyShape] =>
                println("Shape is AnyShape, getting or creating schema")
                getOrCreateSchema(shape.asInstanceOf[AnyShape], validationProcessor)
              case _ =>
                println("Shape is not AnyShape, returning validation error")
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
            case Right(Some(schema)) =>
              println("Schema obtained successfully, calling validator")
              callValidator(schema, obj, fragmentOption, validationProcessor)
            case Left(result) =>
              println("Error occurred during schema generation, returning result")
              result
            case _ =>
              println("No schema or error, returning empty results")
              validationProcessor.processResults(Nil)
          }
        } catch {
          // TODO CHECKEAR SI HAY ALGUNA EXCEPTION ESPECIAL DEVUELTA POR EL VALIDADOR ACA
          case e: Exception =>
            println(s"Exception caught during validation: ${e.getMessage}")
            validationProcessor.processException(e, fragmentOption.map(_.encodes))
        }

      case _ =>
        println("No valid payload or results, returning empty results")
        validationProcessor.processResults(Nil) // ignore
    }
  }

  private def buildCandidate(mediaType: String, payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    buildPayloadObj(mediaType, payload)
  }

  private def buildCandidate(payload: PayloadFragment): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    (loadDataNodeString(payload), Some(PayloadParsingResult(payload, Nil)))
  }

}

trait AvroSchemaReportValidationProcessor extends ReportValidationProcessor {
  override def processCommonException(r: Throwable, element: Option[DomainElement]): Seq[AMFValidationResult] = {
    r match {
      // todo: catch and process each specific exception like the JsonSchemaReportValidationProcessor does in json-schema
      case other =>
        super.processCommonException(other, element)
    }
  }
}
