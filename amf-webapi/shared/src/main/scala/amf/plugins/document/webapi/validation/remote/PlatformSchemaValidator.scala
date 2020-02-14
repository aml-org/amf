package amf.plugins.document.webapi.validation.remote

import amf.client.plugins.{ScalarRelaxedValidationMode, ValidationMode}
import amf.core.annotations.LexicalInformation
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DataNode, ObjectNode, ScalarNode, Shape}
import amf.core.parser.{DefaultParserSideErrorHandler, ErrorHandler, ParserContext, SyamlParsedDocument}
import amf.core.validation._
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.plugins.document.webapi.PayloadPlugin
import amf.plugins.document.webapi.contexts.{JsonSchemaEmitterContext, PayloadContext}
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels.DataTypeFragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.plugins.document.webapi.parser.spec.oas.JsonSchemaValidationFragmentEmitter
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.shapes.models._
import amf.plugins.features.validation.ParserSideValidations.ExampleValidationErrorSpecification
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.{ProfileName, ProfileNames}
import org.yaml.builder.YDocumentBuilder
import org.yaml.model._
import org.yaml.parser.{JsonParser, YamlParser}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class InvalidJsonObject(e: Throwable)       extends RuntimeException(e)
class UnknownDiscriminator()                extends RuntimeException
class UnsupportedMediaType(msg: String)     extends Exception(msg)

abstract class PlatformPayloadValidator(shape: Shape) extends PayloadValidator {

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

  val polymorphic: Boolean = shape match {
    case a: AnyShape => a.supportsInheritance
    case _           => false
  }

  val env = Environment()

  protected val schemas: mutable.Map[String, LoadedSchema] = mutable.Map()

  protected def callValidator(schema: LoadedSchema,
                              obj: LoadedObj,
                              fragment: Option[PayloadFragment],
                              validationProcessor: ValidationProcessor): validationProcessor.Return

  protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj]

  protected def loadJson(text: String): LoadedObj

  protected def loadSchema(jsonSchema: CharSequence): Option[LoadedSchema]

  protected def validateForFragment(fragment: PayloadFragment,
                                    validationProcessor: ValidationProcessor): ValidationProcessor#Return = {

    try {
      performValidation(buildCandidate(fragment), validationProcessor)
    } catch {
      case e: InvalidJsonObject => validationProcessor.processException(e, Some(fragment))
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

  private def generateShape(fragmentShape: Shape): Option[LoadedSchema] = {
    val dataType = DataTypeFragment()
    dataType.fields
      .setWithoutId(DataTypeFragmentModel.Encodes, fragmentShape) // careful, we don't want to modify the ID

    SYamlSyntaxPlugin
      .unparse(
        "application/json",
        SyamlParsedDocument(
          document = new JsonSchemaValidationFragmentEmitter(dataType)(
            JsonSchemaEmitterContext(DefaultParserSideErrorHandler(dataType)))
            .emitFragment())
      )
      .flatMap(loadSchema) // todo for logic in serializer. Hoy to handle the root?
  }

  protected def literalRepresentation(payload: PayloadFragment): Option[String] = {
    val futureText = payload.raw match {
      case Some("") => None
      case _ =>
        val y = new YDocumentBuilder
        if (PayloadPlugin.emit(payload, y)) {
          SYamlSyntaxPlugin.unparse("application/json", SyamlParsedDocument(y.document)) match {
            case Some(serialized) => Some(serialized.toString)
            case _                => None
          }
        } else None

    }

    futureText map { text =>
      payload.encodes match {
        case node: ScalarNode
            if node.dataType.getOrElse("") == (Namespace.Xsd + "string").iri() && text.nonEmpty && text.head != '"' =>
          "\"" + text.stripLineEnd + "\""
        case _ => text.stripLineEnd
      }
    }
  }

  private def buildObjPolymorphicShape(payload: DataNode): Option[LoadedSchema] =
    getOrCreateObj(PolymorphicShapeExtractor(shape.asInstanceOf[AnyShape], payload)) // if is polymorphic is an any shape

  private def getOrCreateObj(s: AnyShape): Option[LoadedSchema] = {
    schemas.get(s.id) match {
      case Some(json) => Some(json)
      case _ =>
        val maybeSchema = generateShape(s)
        maybeSchema.foreach { schemas.put(s.id, _) }
        maybeSchema
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

    val errorHandler = PayloadErrorHandler()
    PayloadParsingResult(parsePayload(payload, mediaType, errorHandler), errorHandler.errors.toList)
  }

  private def parsePayload(payload: String, mediaType: String, errorHandler: ErrorHandler): PayloadFragment = {
    val defaultCtx = new PayloadContext("", Nil, ParserContext(), eh = Some(errorHandler))

    val parser = mediaType match {
      case "application/json" => JsonParser(payload)(errorHandler)
      case _                  => YamlParser(payload)(errorHandler)
    }
    parser.parse(keepTokens = true).collectFirst { case doc: YDocument => doc.node } match {
      case Some(node: YNode) =>
        PayloadFragment(DataNodeParser(node)(defaultCtx).parse(), mediaType)
      case None => PayloadFragment(ScalarNode(payload, None), mediaType)
    }
  }
  case class PayloadErrorHandler(errors: ListBuffer[AMFValidationResult] = ListBuffer()) extends ErrorHandler {
    override def reportConstraint(id: String,
                                  node: String,
                                  property: Option[String],
                                  message: String,
                                  lexical: Option[LexicalInformation],
                                  level: String,
                                  location: Option[String]): Unit =
      handleAmfResult(AMFValidationResult(message, level, node, property, id, lexical, location, this))

    def handleAmfResult(result: AMFValidationResult): Unit = synchronized {
      if (!errors.exists(v => v.equals(result))) {
        errors += result
      }
    }
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
              case Some(result) if polymorphic =>
                Right(buildObjPolymorphicShape(result.fragment.encodes)) // if is polymorphic I already parse the payload
              case _ if shape.isInstanceOf[AnyShape] => Right(getOrCreateObj(shape.asInstanceOf[AnyShape]))
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
          case e: UnknownDiscriminator => validationProcessor.processException(e, fragmentOption)
        }

      case _ => validationProcessor.processResults(Nil) // ignore
    }
  }

  private def buildCandidate(mediaType: String, payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    if (isFileShape) {
      (None, None)
    } else if (polymorphic) {
      buildPayloadNode(mediaType, payload)
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

object PolymorphicShapeExtractor {

  def apply(anyShape: AnyShape, payload: DataNode): AnyShape = {
    val closure: Seq[Shape] = anyShape.effectiveStructuralShapes
    findPolymorphicEffectiveShape(closure, payload) match {
      case Some(shape: NodeShape) => shape
      case _ => // TODO: structurally can be a valid type, should we fail? By default RAML expects a failure
        throw new UnknownDiscriminator()
    }
  }

  private def findPolymorphicEffectiveShape(polymorphicUnion: Seq[Shape], currentDataNode: DataNode): Option[Shape] = {
    polymorphicUnion.filter(_.isInstanceOf[NodeShape]).find {
      case nodeShape: NodeShape =>
        nodeShape.discriminator.option() match {
          case Some(discriminatorProp) =>
            val discriminatorValue = nodeShape.discriminatorValue.option().getOrElse(nodeShape.name.value())
            currentDataNode match {
              case obj: ObjectNode =>
                obj.properties.get(discriminatorProp) match {
                  case Some(v: ScalarNode) =>
                    v.value == discriminatorValue
                  case _ => false
                }
              case _ => false
            }
          case None => false
        }
    }
  }
}

object ScalarPayloadForParam {

  def apply(fragment: PayloadFragment, shape: Shape): PayloadFragment = {
    if (isString(shape) || unionWithString(shape)) {

      fragment.encodes match {
        case s: ScalarNode if !s.dataType.getOrElse("").equals((Namespace.Xsd + "string").iri()) =>
          PayloadFragment(ScalarNode(s.value, Some((Namespace.Xsd + "string").iri()), s.annotations),
                          fragment.mediaType.value())
        case _ => fragment
      }
    } else fragment
  }

  private def isString(shape: Shape): Boolean = shape match {
    case s: ScalarShape => s.dataType.option().exists(_.equals((Namespace.Xsd + "string").iri()))
    case _              => false
  }

  private def unionWithString(shape: Shape): Boolean = shape match {
    case u: UnionShape => u.anyOf.exists(isString)
    case _             => false
  }

}
