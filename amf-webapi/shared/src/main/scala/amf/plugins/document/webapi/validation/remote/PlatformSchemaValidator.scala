package amf.plugins.document.webapi.validation.remote

import amf.client.parse.DefaultParserErrorHandler
import amf.client.plugins.{ScalarRelaxedValidationMode, ValidationMode}
import amf.core.client.ParsingOptions
import amf.core.emitter.ShapeRenderOptions
import amf.core.model.DataType
import amf.core.model.document.PayloadFragment
import amf.core.model.domain._
import amf.core.parser.errorhandler.AmfParserErrorHandler
import amf.core.parser.{ParserContext, SyamlParsedDocument}
import amf.core.validation._
import amf.internal.environment.Environment
import amf.plugins.document.webapi.PayloadPlugin
import amf.plugins.document.webapi.contexts.emitter.oas.JsonSchemaEmitterContext
import amf.plugins.document.webapi.contexts.parser.raml.PayloadContext
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels.DataTypeFragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.{DataNodeParser, RefCounter}
import amf.plugins.document.webapi.parser.spec.oas.JsonSchemaValidationFragmentEmitter
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.shapes.models._
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.validations.PayloadValidations.ExampleValidationErrorSpecification
import amf.{ProfileName, ProfileNames}
import org.yaml.builder.YDocumentBuilder
import org.yaml.model._
import org.yaml.parser.{JsonParser, YamlParser}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class InvalidJsonObject(e: Throwable)       extends RuntimeException(e)
class UnknownDiscriminator()                extends RuntimeException
class UnsupportedMediaType(msg: String)     extends Exception(msg)

abstract class PlatformPayloadValidator(shape: Shape, env: Environment) extends PayloadValidator {

  override val defaultSeverity: String = SeverityLevels.VIOLATION
  protected def getReportProcessor(profileName: ProfileName): ValidationProcessor

  override def isValid(mediaType: String, payload: String): Future[Boolean] = {
    Future(validateForPayload(mediaType, payload, BooleanValidationProcessor))
  }

  override def validate(mediaType: String, payload: String): Future[AMFValidationReport] = {
    Future(
      validateForPayload(mediaType, payload, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport])
  }

  override def validate(fragment: PayloadFragment): Future[AMFValidationReport] = {
    Future(validateForFragment(fragment, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport])
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
    }
  }

  /* i need to do this check?? */
  protected def validateForPayload(mediaType: String,
                                   payload: String,
                                   validationProcessor: ValidationProcessor): validationProcessor.Return = {
    if (!PayloadValidatorPlugin.payloadMediaType.contains(mediaType)) {
      validationProcessor.processResults(
        Seq(
          AMFValidationResult(
            s"Unsupported payload media type '$mediaType', only ${PayloadValidatorPlugin.payloadMediaType.toString()} supported",
            SeverityLevels.VIOLATION,
            "",
            None,
            ExampleValidationErrorSpecification.id,
            None,
            None,
            null
          )))
    } else {
      try {
        performValidation(buildCandidate(mediaType, payload), validationProcessor)
      } catch {
        case e: InvalidJsonObject =>
          validationProcessor.processException(e, None)
      }

    }
  }

  private def generateShape(
      fragmentShape: Shape,
      validationProcessor: ValidationProcessor): Either[validationProcessor.Return, Option[LoadedSchema]] = {
    val dataType = DataTypeFragment()
    dataType.fields
      .setWithoutId(DataTypeFragmentModel.Encodes, fragmentShape) // careful, we don't want to modify the ID

    val unparsedDocOption = SYamlSyntaxPlugin
      .unparse(
        "application/json",
        SyamlParsedDocument(
          document = new JsonSchemaValidationFragmentEmitter(dataType)(
            JsonSchemaEmitterContext(dataType.errorHandler(), new ShapeRenderOptions().withoutDocumentation))
            .emitFragment())
      )

    unparsedDocOption match {
      case Some(charSequence) => loadSchema(charSequence, fragmentShape, validationProcessor)
      case None               => Right(None)
    }
  }

  protected def literalRepresentation(payload: PayloadFragment): Option[String] = {
    val futureText = payload.raw match {
      case Some("") => None
      case _ =>
        val y = new YDocumentBuilder
        if (PayloadPlugin.emit(payload, y)) {
          SYamlSyntaxPlugin.unparse("application/json", SyamlParsedDocument(y.document.asInstanceOf[YDocument])) match {
            case Some(serialized) => Some(serialized.toString)
            case _                => None
          }
        } else None

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

  private def getOrCreateObj(
      s: AnyShape,
      validationProcessor: ValidationProcessor): Either[validationProcessor.Return, Option[LoadedSchema]] = {
    schemas.get(s.id) match {
      case Some(json) => Right(Some(json))
      case _ =>
        generateShape(s, validationProcessor) match {
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

  def parsePayloadWithErrorHandler(payload: String,
                                   mediaType: String,
                                   env: Environment,
                                   shape: Shape): PayloadParsingResult = {

    val errorHandler = DefaultParserErrorHandler.withRun()
    PayloadParsingResult(parsePayload(payload, mediaType, errorHandler), errorHandler.getErrors)
  }

  private def parsePayload(payload: String, mediaType: String, errorHandler: AmfParserErrorHandler): PayloadFragment = {
    val options = ParsingOptions()
    env.maxYamlReferences.foreach(options.setMaxYamlReferences)
    val defaultCtx = new PayloadContext("", Nil, ParserContext(eh = errorHandler), options = options)

    val parser = mediaType match {
      case "application/json" => JsonParser(payload)(errorHandler)
      case _                  => YamlParser(payload)(errorHandler)
    }
    val node = parser.document().node
    PayloadFragment(if (node.isNull) ScalarNode(payload, None) else DataNodeParser(node)(defaultCtx).parse(),
                    mediaType)
  }

  protected def buildPayloadNode(mediaType: String, payload: String): (Option[LoadedObj], Some[PayloadParsingResult]) = {
    val fixedResult = parsePayloadWithErrorHandler(payload, mediaType, env, shape) match {
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
                getOrCreateObj(shape.asInstanceOf[AnyShape], validationProcessor)
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
