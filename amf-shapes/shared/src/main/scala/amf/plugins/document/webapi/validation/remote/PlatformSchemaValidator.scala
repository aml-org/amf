package amf.plugins.document.webapi.validation.remote

import amf.client.parse.DefaultParserErrorHandler
import amf.client.plugins.{ScalarRelaxedValidationMode, ValidationMode}
import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.client.render.JsonSchemaDraft7
import amf.core.client.ParsingOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.DataType
import amf.core.model.document.PayloadFragment
import amf.core.model.domain._
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.errorhandler.AmfParserErrorHandler
import amf.core.parser.{ErrorHandlingContext, FragmentRef, JsonParserFactory, ParsedReference, SearchScope, SyamlParsedDocument}
import amf.core.validation._
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.parser.spec.common.{DataNodeParser, DataNodeParserContext, JsonSchemaEmitter, PayloadEmitter}
import amf.plugins.document.webapi.validation.remote.PlatformPayloadValidator.supportedMediaTypes
import amf.plugins.domain.shapes.models._
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.validations.ShapePayloadValidations.ExampleValidationErrorSpecification
import amf.{ProfileName, ProfileNames}
import org.yaml.parser.YamlParser

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class InvalidJsonObject(e: Throwable)       extends RuntimeException(e)
class InvalidJsonValue(e: Throwable)        extends RuntimeException(e)
class UnknownDiscriminator()                extends RuntimeException
class UnsupportedMediaType(msg: String)     extends Exception(msg)

object PlatformPayloadValidator {
  val supportedMediaTypes: Seq[String] = Seq("application/json", "application/yaml", "text/vnd.yaml")
}

abstract class PlatformPayloadValidator(shape: Shape, configuration: ValidationConfiguration) extends PayloadValidator {

  override val defaultSeverity: String = SeverityLevels.VIOLATION
  protected def getReportProcessor(profileName: ProfileName): ValidationProcessor

  override def isValid(mediaType: String, payload: String)(
      implicit executionContext: ExecutionContext): Future[Boolean] = {
    Future(validateForPayload(mediaType, payload, BooleanValidationProcessor))
  }

  override def validate(mediaType: String, payload: String)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    Future(
      validateForPayload(mediaType, payload, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport])
  }

  override def validate(fragment: PayloadFragment)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    Future(validateForFragment(fragment, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport])
  }

  override def syncValidate(mediaType: String, payload: String): AMFValidationReport = {
    validateForPayload(mediaType, payload, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport]
  }

  type LoadedObj
  type LoadedSchema
  val validationMode: ValidationMode

  val isFileShape: Boolean = shape.isInstanceOf[FileShape]

  protected val schemas: mutable.Map[String, LoadedSchema] = mutable.Map()

  protected def callValidator(schema: LoadedSchema,
                              obj: LoadedObj,
                              fragment: Option[PayloadFragment],
                              validationProcessor: ValidationProcessor): validationProcessor.Return

  protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj]

  protected def loadJson(text: String): LoadedObj

  protected def loadJsonSchema(text: String): LoadedObj = loadJson(text)

  protected def loadSchema(
      jsonSchema: CharSequence,
      element: DomainElement,
      validationProcessor: ValidationProcessor): Either[validationProcessor.Return, Option[LoadedSchema]]

  protected def validateForFragment(fragment: PayloadFragment,
                                    validationProcessor: ValidationProcessor): ValidationProcessor#Return = {

    try {
      performValidation(buildCandidate(fragment), validationProcessor)
    } catch {
      case e: InvalidJsonObject => validationProcessor.processException(e, Some(fragment.encodes))
      case e: InvalidJsonValue  => validationProcessor.processException(e, Some(fragment.encodes))
    }
  }

  /* i need to do this check?? */
  protected def validateForPayload(mediaType: String,
                                   payload: String,
                                   validationProcessor: ValidationProcessor): validationProcessor.Return = {
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
          )))
    } else
      shape match {
        // if the shape is of type any, any payload should validate against it so the validation is skipped
        case anyShape: AnyShape if anyShape.isAnyType => validationProcessor.processResults(Nil)
        case _ =>
          try {
            performValidation(buildCandidate(mediaType, payload), validationProcessor)
          } catch {
            case e: InvalidJsonObject => validationProcessor.processException(e, None)
            case e: InvalidJsonValue  => validationProcessor.processException(e, None)
          }
      }
  }

  private def generateSchema(
      fragmentShape: Shape,
      validationProcessor: ValidationProcessor): Either[validationProcessor.Return, Option[LoadedSchema]] = {

    val schemaOption: Option[CharSequence] = generateSchemaString(fragmentShape, validationProcessor)

    schemaOption match {
      case Some(charSequence) => loadSchema(charSequence, fragmentShape, validationProcessor)
      case None               => Right(None)
    }
  }

  private def generateSchemaString(shape: Shape,
                                   validationProcessor: ValidationProcessor): Option[CharSequence] = {
    val renderOptions = ShapeRenderOptions().withoutDocumentation.withCompactedEmission
      .withSchemaVersion(JsonSchemaDraft7)
      .withEmitWarningForUnsupportedValidationFacets(true)
    val declarations = List(shape)
    val emitter =
      JsonSchemaEmitter(shape, declarations, options = renderOptions, errorHandler = configuration.eh)
    val document = SyamlParsedDocument(document = emitter.emitDocument())
    validationProcessor.keepResults(configuration.eh.results())
    SYamlSyntaxPlugin.unparse("application/json", document)
  }

  protected def literalRepresentation(payload: PayloadFragment): Option[String] = {
    val futureText = payload.raw match {
      case Some("") => None
      case _ =>
        val document = PayloadEmitter(payload.encodes)(UnhandledErrorHandler).emitDocument()
        SYamlSyntaxPlugin.unparse("application/json", SyamlParsedDocument(document)).map(_.toString)
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
      validationProcessor: ValidationProcessor): Either[validationProcessor.Return, Option[LoadedSchema]] = {
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

  protected def buildPayloadObj(mediaType: String,
                                payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    if (mediaType == "application/json" && validationMode != ScalarRelaxedValidationMode)
      (Some(loadJson(payload)), None)
    else {
      buildPayloadNode(mediaType, payload)
    }
  }

  def parsePayloadWithErrorHandler(payload: String, mediaType: String, shape: Shape): PayloadParsingResult = {

    val errorHandler = configuration.eh
    PayloadParsingResult(parsePayload(payload, mediaType, DefaultParserErrorHandler.fromErrorHandler(errorHandler)),
                         errorHandler.results())
  }

  private def parsePayload(payload: String, mediaType: String, errorHandler: AmfParserErrorHandler): PayloadFragment = {
    val options = ParsingOptions()
    configuration.maxYamlReferences.foreach(options.setMaxYamlReferences)
    val ctx = dataNodeParsingCtx(errorHandler, options.getMaxYamlReferences)

    val parser = mediaType match {
      case "application/json" => JsonParserFactory.fromChars(payload)(errorHandler)
      case _                  => YamlParser(payload)(errorHandler)
    }
    val node = parser.document().node
    val parsedNode = if (node.isNull) ScalarNode(payload, None).withDataType(DataType.Nil)
                      else DataNodeParser(node)(ctx).parse()
    PayloadFragment(parsedNode, mediaType)
  }

  private def dataNodeParsingCtx(errorHandler: AmfParserErrorHandler, maxYamlRefs: Option[Long]): ErrorHandlingContext with DataNodeParserContext = {
    new ErrorHandlingContext()(errorHandler) with DataNodeParserContext {
      override def violation(violationId: ValidationSpecification, node: String, message: String): Unit =
        eh.violation(violationId, node, message, "")
      override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] = None
      override def refs: Seq[ParsedReference] = Seq.empty
      override def getMaxYamlReferences: Option[Long] = maxYamlRefs
      override def fragments: Map[String, FragmentRef] = Map.empty
    }
  }

  protected def buildPayloadNode(mediaType: String, payload: String): (Option[LoadedObj], Some[PayloadParsingResult]) = {
    val fixedResult = parsePayloadWithErrorHandler(payload, mediaType, shape) match {
      case result if !result.hasError && validationMode == ScalarRelaxedValidationMode =>
        val frag = ScalarPayloadForParam(result.fragment, shape)
        result.copy(fragment = frag)
      case other => other
    }
    if (!fixedResult.hasError) (loadDataNodeString(fixedResult.fragment), Some(fixedResult))
    else (None, Some(fixedResult))
  }

  private def performValidation(payload: (Option[LoadedObj], Option[PayloadParsingResult]),
                                validationProcessor: ValidationProcessor): validationProcessor.Return = {
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
                  validationProcessor.processResults(Seq(AMFValidationResult(
                    "Cannot validate shape that is not an any shape",
                    defaultSeverity,
                    "",
                    Some(shape.id),
                    ExampleValidationErrorSpecification.id,
                    shape.position(),
                    shape.location(),
                    null
                  ))))
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
          PayloadFragment(ScalarNode(s.value.value(), Some(DataType.String), s.annotations),
                          fragment.mediaType.value())
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
