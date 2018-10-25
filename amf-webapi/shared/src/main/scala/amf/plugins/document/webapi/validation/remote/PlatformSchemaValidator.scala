package amf.plugins.document.webapi.validation.remote

import amf.client.plugins.PayloadParsingResult
import amf.core.emitter.RenderOptions
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DataNode, ObjectNode, ScalarNode, Shape}
import amf.core.parser.SyamlParsedDocument
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels}
import amf.core.vocabulary.Namespace
import amf.internal.environment.Environment
import amf.plugins.document.webapi.PayloadPlugin
import amf.plugins.document.webapi.contexts.JsonSchemaEmitterContext
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels.DataTypeFragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.oas.JsonSchemaValidationFragmentEmitter
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.shapes.models.{AnyShape, FileShape, NodeShape}
import amf.plugins.domain.shapes.validation.ScalarPayloadForParam
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.{ProfileName, ProfileNames}

import scala.collection.mutable

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class InvalidJsonObject(e: Throwable)       extends RuntimeException(e)
class UnknownDiscriminator()                extends RuntimeException
class UnsupportedMediaType(msg: String)     extends Exception(msg)

abstract class PlatformPayloadValidator(shape: AnyShape) extends PlatformSchemaValidator {

  protected def getReportProcessor(profileName: ProfileName): ValidationProcessor

  def fastValidation(mediaType: String, payload: String): Boolean = {
    validateForPayload(mediaType, payload, BooleanValidationProcessor)
  }

  def validate(mediaType: String, payload: String): AMFValidationReport = {
    validateForPayload(mediaType, payload, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport]
  }

  def validate(fragment: PayloadFragment): AMFValidationReport = {
    validateForFragment(fragment, getReportProcessor(ProfileNames.AMF)).asInstanceOf[AMFValidationReport]
  }

}

/* trait for all platform natives validations (payload and schema) */
trait PlatformSchemaValidator {

  type LoadedObj
  type LoadedSchema
  val shape: AnyShape

  val isFileShape: Boolean = shape.isInstanceOf[FileShape]
  val polymorphic: Boolean = shape.supportsInheritance

  private val env = Environment()

  protected val schemas: mutable.Map[String, LoadedSchema] = mutable.Map()

  protected def callValidator(schema: LoadedSchema,
                              obj: LoadedObj,
                              fragment: Option[PayloadFragment],
                              validationProcessor: ValidationProcessor): validationProcessor.Return

  protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj]

  protected def loadJson(text: String): LoadedObj

  protected def loadSchema(jsonSchema: String): Option[LoadedSchema]

  protected def validateForFragment(fragment: PayloadFragment,
                                    validationProcessor: ValidationProcessor): ValidationProcessor#Return = {

    try {
      performValidation(buildCandidate(fragment), validationProcessor)
    } catch {
      case e: InvalidJsonObject => validationProcessor.processException(e, Some(fragment))
    }
  }

  protected def validateForPayload(mediaType: String,
                                   payload: String,
                                   validationProcessor: ValidationProcessor): validationProcessor.Return = {
    if (mediaType != "application/json" && mediaType != "application/yaml") {
      validationProcessor.processResults(
        Seq(
          AMFValidationResult(
            s"Unsupported payload media type '$mediaType', only application/json and application/yaml supported",
            SeverityLevels.VIOLATION,
            "",
            None,
            (Namespace.AmfParser + "example-validation-error").iri(),
            None,
            None,
            null
          )))
    } else {
      try {

        performValidation(buildCandidate(mediaType, payload), validationProcessor)
      } catch {
        case e: InvalidJsonObject => validationProcessor.processException(e, None)
      }

    }
  }

  private def generateShape(fragmentShape: Shape): Option[LoadedSchema] = {
    val dataType = DataTypeFragment()
    dataType.fields
      .setWithoutId(DataTypeFragmentModel.Encodes, fragmentShape) // careful, we don't want to modify the ID

    SYamlSyntaxPlugin
      .unparse("application/json",
               SyamlParsedDocument(
                 document = new JsonSchemaValidationFragmentEmitter(dataType)(JsonSchemaEmitterContext())
                   .emitFragment()))
      .flatMap(loadSchema) // todo for logic in serializer. Hoy to handle the root?
  }

  protected def literalRepresentation(payload: PayloadFragment): Option[String] = {
    val futureText = payload.raw match {
      case Some("") => None
      case _ =>
        PayloadPlugin.unparse(payload, RenderOptions()) match {
          case Some(doc) =>
            SYamlSyntaxPlugin.unparse("application/json", doc) match {
              case Some(serialized) => Some(serialized)
              case _                => None
            }
          case _ => None
        }
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
    getOrCreateObj(PolymorphicShapeExtractor(shape, payload))

  private def getOrCreateObj(s: AnyShape): Option[LoadedSchema] = {
    schemas.get(s.id) match {
      case Some(json) => Some(json)
      case _          => generateShape(s)
    }
  }

  protected def buildPayloadObj(mediaType: String,
                                payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    if (mediaType == "application/json") (Some(loadJson(payload)), None)
    else {
      buildPayloadNode(mediaType, payload)
    }
  }

  protected def buildPayloadNode(mediaType: String, payload: String): (Option[LoadedObj], Some[PayloadParsingResult]) = {
    val payloadResult = PayloadValidatorPlugin.parsePayloadWithErrorHandler(payload, mediaType, env, shape)
    if (!payloadResult.hasError) (loadDataNodeString(payloadResult.fragment), Some(payloadResult))
    else (None, Some(payloadResult))
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
                buildObjPolymorphicShape(result.fragment.encodes) // if is polymorphic I already parse the payload
              case _ => getOrCreateObj(shape)
            }
          } match {
            case Some(schema) => callValidator(schema, obj, fragmentOption, validationProcessor)
            case _            => validationProcessor.processResults(Nil)
          }
        } catch {
          case e: UnknownDiscriminator =>
            println("processing uknown discriminator at perform validation")
            validationProcessor.processException(e, fragmentOption)
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

trait ParameterValidator extends PlatformPayloadValidator {
  override protected def buildPayloadObj(mediaType: String,
                                         payload: String): (Option[LoadedObj], Option[PayloadParsingResult]) = {
    buildPayloadNode(mediaType, payload) match {
      case (obj, Some(result)) if !result.hasError =>
        val frag = ScalarPayloadForParam(result.fragment, shape)
        (obj, Some(result.copy(fragment = frag)))
      case other => other
    }
  }
}

object PolymorphicShapeExtractor {

  def apply(anyShape: AnyShape, payload: DataNode): AnyShape = {
    val closure: Seq[Shape] = anyShape.effectiveStructuralShapes
    findPolymorphicEffectiveShape(closure, payload) match {
      case Some(shape: NodeShape) => shape
      case _ => // TODO: structurally can be a valid type, should we fail? By default RAML expects a failure
        println("throwing exception at polymorpich extractor")
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
