package amf.shapes.internal.validation.jsonschema

import amf.core.client.common.render.JsonSchemaDraft7
import amf.core.client.common.validation._
import amf.core.client.scala.config.{ParsingOptions, RenderOptions}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.parse.document.{ErrorHandlingContext, ParsedReference, SyamlParsedDocument}
import amf.core.client.scala.validation.payload.{
  AMFShapePayloadValidator,
  PayloadParsingResult,
  ShapeValidationConfiguration
}
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.internal.datanode.{DataNodeParser, DataNodeParserContext}
import amf.core.internal.parser.domain.{FragmentRef, JsonParserFactory, SearchScope}
import amf.core.internal.plugins.syntax.{SYamlAMFParserErrorHandler, SyamlSyntaxRenderPlugin}
import amf.core.internal.remote.Mimes._
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.{AnyShape, FileShape, ScalarShape, UnionShape}
import amf.shapes.internal.spec.common.emitter.PayloadEmitter
import amf.shapes.internal.spec.jsonschema.emitter.JsonSchemaEmitter
import amf.shapes.internal.validation.definitions.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.shapes.internal.validation.jsonschema.BaseJsonSchemaPayloadValidator.supportedMediaTypes
import amf.shapes.internal.validation.payload.MaxNestingValueReached
import org.yaml.model.{IllegalTypeHandler, YError}
import org.yaml.parser.YamlParser

import java.io.StringWriter
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class InvalidJsonObject(e: Throwable)       extends RuntimeException(e)
class InvalidJsonValue(e: Throwable)        extends RuntimeException(e)
class UnknownDiscriminator()                extends RuntimeException
class UnsupportedMediaType(msg: String)     extends Exception(msg)

object BaseJsonSchemaPayloadValidator {
  val supportedMediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`, `text/vnd.yaml`)
}

abstract class BaseJsonSchemaPayloadValidator(
    shape: Shape,
    mediaType: String,
    configuration: ShapeValidationConfiguration
) extends AMFShapePayloadValidator {

  private val defaultSeverity: String = SeverityLevels.VIOLATION
  protected def getReportProcessor(profileName: ProfileName): ValidationProcessor
  protected implicit val executionContext: ExecutionContext = configuration.executionContext

  override def validate(payload: String): Future[AMFValidationReport] = {
    Future.successful(
      validateForPayload(payload, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport]
    )
  }

  override def validate(fragment: PayloadFragment): Future[AMFValidationReport] = {
    Future.successful(
      validateForFragment(fragment, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport]
    )
  }

  override def syncValidate(payload: String): AMFValidationReport = {
    validateForPayload(payload, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport]
  }

  protected type LoadedObj
  protected type LoadedSchema
  protected val validationMode: ValidationMode

  private val isFileShape: Boolean = shape.isInstanceOf[FileShape]
  private val isAnyType: Boolean = shape match {
    case as: AnyShape if as.isAnyType => true
    case _                            => false
  }

  protected val schemas: mutable.Map[String, LoadedSchema] = mutable.Map()

  protected def callValidator(
      schema: LoadedSchema,
      obj: LoadedObj,
      fragment: Option[PayloadFragment],
      validationProcessor: ValidationProcessor
  ): validationProcessor.Return

  protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj]

  protected def loadJson(text: String): LoadedObj

  protected def loadJsonSchema(text: String): LoadedObj = loadJson(text)

  protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor
  ): Either[validationProcessor.Return, Option[LoadedSchema]]

  protected def validateForFragment(
      fragment: PayloadFragment,
      validationProcessor: ValidationProcessor
  ): ValidationProcessor#Return = {

    try {
      performValidation(buildCandidate(fragment), validationProcessor)
    } catch {
      case e: InvalidJsonObject      => validationProcessor.processException(e, Some(fragment.encodes))
      case e: InvalidJsonValue       => validationProcessor.processException(e, Some(fragment.encodes))
      case e: MaxNestingValueReached => validationProcessor.processException(e, None)
    }
  }

  /* i need to do this check?? */
  protected def validateForPayload(
      payload: String,
      validationProcessor: ValidationProcessor
  ): validationProcessor.Return = {
    if (!supportedMediaTypes.contains(mediaType)) {
      validationProcessor.processResults(
        Seq(
          AMFValidationResult(
            s"Unsupported payload media type '$mediaType', only ${supportedMediaTypes.toString()} supported",
            SeverityLevels.VIOLATION,
            "",
            None,
            ExampleValidationErrorSpecification.id,
            None,
            None,
            null
          )
        )
      )
    } else
      try {
        performValidation(buildCandidate(mediaType, payload), validationProcessor)
      } catch {
        case e: InvalidJsonObject => validationProcessor.processException(e, None)
        // if the shape is of type any, any scalar payload should validate against it so the validation is skipped
        // We don't skip completely the validation because if the payload is an object with an error we want the error
        case e: InvalidJsonValue if isAnyType => validationProcessor.processResults(Nil)
        case e: InvalidJsonValue              => validationProcessor.processException(e, None)
        case e: MaxNestingValueReached        => validationProcessor.processException(e, None)
      }
  }

  private def generateSchema(
      fragmentShape: Shape,
      validationProcessor: ValidationProcessor
  ): Either[validationProcessor.Return, Option[LoadedSchema]] = {

    val schemaOption: Option[CharSequence] = generateSchemaString(fragmentShape, validationProcessor)

    schemaOption match {
      case Some(charSequence) => loadSchema(charSequence, fragmentShape, validationProcessor)
      case None               => Right(None)
    }
  }

  private def generateSchemaString(shape: Shape, validationProcessor: ValidationProcessor): Option[CharSequence] = {
    val renderOptions = RenderOptions().withoutDocumentation
      .withSchemaVersion(JsonSchemaDraft7)
      .withEmitWarningForUnsupportedValidationFacets(true)
    val declarations = List(shape)
    val eh           = configuration.eh()
    val emitter      = JsonSchemaEmitter(renderOptions, eh)
    val YDocument    = emitter.emit(shape, declarations)
    val document     = SyamlParsedDocument(YDocument)
    validationProcessor.keepResults(eh.getResults)
    val writer = new StringWriter()
    SyamlSyntaxRenderPlugin.emit(`application/json`, document, writer).map(_.toString)
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

  private def getOrCreateSchema(
      s: AnyShape,
      validationProcessor: ValidationProcessor
  ): Either[validationProcessor.Return, Option[LoadedSchema]] = {
    schemas.get(s.id) match {
      case Some(json) => Right(Some(json))
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
      (Some(loadJson(payload)), None)
    else {
      buildPayloadNode(mediaType, payload)
    }
  }

  private def parsePayloadWithErrorHandler(payload: String, mediaType: String, shape: Shape): PayloadParsingResult = {

    val errorHandler = configuration.eh()
    PayloadParsingResult(parsePayload(payload, mediaType, errorHandler), errorHandler.getResults)
  }

  private def parsePayload(payload: String, mediaType: String, errorHandler: AMFErrorHandler): PayloadFragment = {
    val options = ParsingOptions()
    configuration.maxYamlReferences.foreach(options.setMaxYamlReferences)
    val ctx = dataNodeParsingCtx(errorHandler, options.getMaxYamlReferences)

    val parser = mediaType match {
      case `application/json` => JsonParserFactory.fromChars(payload)(errorHandler)
      case _                  => YamlParser(payload)(new SYamlAMFParserErrorHandler(errorHandler))
    }
    val node = parser.document().node
    val parsedNode =
      if (node.isNull) ScalarNode(payload, None).withDataType(DataType.Nil)
      else DataNodeParser(node)(ctx).parse()
    PayloadFragment(parsedNode, mediaType)
  }

  private def dataNodeParsingCtx(
      errorHandler: AMFErrorHandler,
      maxYamlRefs: Option[Int]
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
      override def fragments: Map[String, FragmentRef]                                                 = Map.empty

      override def handle[T](error: YError, defaultValue: T): T = syamleh.handle(error, defaultValue)
    }
  }

  protected def buildPayloadNode(
      mediaType: String,
      payload: String
  ): (Option[LoadedObj], Some[PayloadParsingResult]) = {
    val fixedResult = parsePayloadWithErrorHandler(payload, mediaType, shape) match {
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
  ): validationProcessor.Return = {
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
            case Right(Some(schema)) => callValidator(schema, obj, fragmentOption, validationProcessor)
            case Left(result)        => result
            case _                   => validationProcessor.processResults(Nil)
          }
        } catch {
          case e: UnknownDiscriminator => validationProcessor.processException(e, fragmentOption.map(_.encodes))
        }

      case _ => validationProcessor.processResults(Nil) // ignore
    }
  }

  private def buildCandidate(mediaType: String, payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    if (isFileShape) {
      (None, None)
    } else {
      buildPayloadObj(mediaType, payload)
    }
  }

  private def buildCandidate(payload: PayloadFragment): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    if (isFileShape) {
      (None, None)
    } else {
      (loadDataNodeString(payload), Some(PayloadParsingResult(payload, Nil)))
    }
  }

}

object ScalarPayloadForParam {

  def apply(fragment: PayloadFragment, shape: Shape): PayloadFragment = {
    if (isString(shape) || unionWithString(shape)) {

      fragment.encodes match {
        case s: ScalarNode if !s.dataType.option().exists(_.equals(DataType.String)) =>
          PayloadFragment(ScalarNode(s.value.value(), Some(DataType.String), s.annotations), fragment.mediaType.value())
        case _ => fragment
      }
    } else fragment
  }

  private def isString(shape: Shape): Boolean = shape match {
    case s: ScalarShape => s.dataType.option().exists(_.equals(DataType.String))
    case _              => false
  }

  private def unionWithString(shape: Shape): Boolean = shape match {
    case u: UnionShape => u.anyOf.exists(isString)
    case _             => false
  }

}
