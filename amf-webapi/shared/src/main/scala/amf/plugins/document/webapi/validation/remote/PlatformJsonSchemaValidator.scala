package amf.plugins.document.webapi.validation.remote

import amf.core.emitter.RenderOptions
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DataNode, ObjectNode, ScalarNode, Shape}
import amf.core.parser.SyamlParsedDocument
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels, ValidationCandidate}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.JsonSchemaEmitterContext
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels.DataTypeFragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.PayloadPlugin
import amf.plugins.document.webapi.parser.spec.oas.JsonSchemaValidationFragmentEmitter
import amf.plugins.domain.shapes.models.{AnyShape, FileShape, NodeShape}
import amf.plugins.syntax.SYamlSyntaxPlugin

import scala.collection.mutable
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class InvalidJsonObject(e: Throwable)       extends RuntimeException(e)
class UnknownDiscriminator()                extends RuntimeException
class UnsupportedMediaType(msg: String)     extends Exception(msg)

abstract class PlatformPayloadValidator(shape: AnyShape) {

  def validate(mediaType: String, payload: String): Boolean

}

/* trait for all platform natives validations (payload and schema) */
trait PlatformSchemaValidator {
  def validate(validationCandidates: Seq[ValidationCandidate], profile: ValidationProfile): Future[AMFValidationReport]

  def findPolymorphicShape(anyShape: AnyShape, payload: DataNode): AnyShape = {
    val closure: Seq[Shape] = anyShape.effectiveStructuralShapes
    findPolymorphicEffectiveShape(closure, payload) match {
      case Some(shape: NodeShape) => shape
      case _                      => // TODO: structurally can be a valid type, should we fail? By default RAML expects a failure
        /*
        val polymorphicUnion = UnionShape().withId(payload.id + "_polymorphic")
        polymorphicUnion.setArrayWithoutId(UnionShapeModel.AnyOf, closure)
        polymorphicUnion
         */
        throw new UnknownDiscriminator()
    }
  }

  def findPolymorphicEffectiveShape(polymorphicUnion: Seq[Shape], currentDataNode: DataNode): Option[Shape] = {
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

  def literalRepresentation(payload: PayloadFragment): Option[String] = {
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
}

/* trait for platform natives validations of paylodas */
trait PlatformJsonSchemaValidator extends PlatformSchemaValidator {

  type LoadedObj
  case class JsonSchemaCandidate(candidate: ValidationCandidate,
                                 jsonSchema: String,
                                 payloadSchema: Option[LoadedObj],
                                 maybeException: Option[Throwable])

  protected def loadDataNodeString(payload: PayloadFragment): Option[LoadedObj]

  protected def processCandidate(obj: LoadedObj, str: String, fragment: PayloadFragment): Seq[AMFValidationResult]

  override def validate(validationCandidates: Seq[ValidationCandidate],
                        profile: ValidationProfile): Future[AMFValidationReport] = Future {

    val jsonSchemaCandidates: Seq[JsonSchemaCandidate] = computeJsonSchemaCandidates(validationCandidates)

    val results: Seq[AMFValidationResult] = jsonSchemaCandidates flatMap {
      case JsonSchemaCandidate(vc, _, _, Some(e: UnknownDiscriminator)) => // error case for polymorphic shapes without matching discriminator
        Seq(
          AMFValidationResult(
            message = "Unknown discriminator value",
            level = SeverityLevels.VIOLATION,
            targetNode = vc.payload.encodes.id,
            targetProperty = None,
            validationId = (Namespace.AmfParser + "example-validation-error").iri(),
            position = vc.payload.encodes.position(),
            location = vc.payload.encodes.location(),
            source = e
          ))
      case JsonSchemaCandidate(vc, _, _, Some(e: InvalidJsonObject)) => // error case for polymorphic shapes without matching discriminator
        Seq(
          AMFValidationResult(
            message = "Unsupported chars in string value (probably a binary file)",
            level = SeverityLevels.VIOLATION,
            targetNode = vc.payload.encodes.id,
            targetProperty = None,
            validationId = (Namespace.AmfParser + "example-validation-error").iri(),
            position = vc.payload.encodes.position(),
            location = vc.payload.encodes.location(),
            source = e
          ))
      case JsonSchemaCandidate(_, _, _, Some(_))               => Nil // we ignore other exceptions
      case JsonSchemaCandidate(c, jsonSchema, Some(obj), None) => processCandidate(obj, jsonSchema, c.payload)
      //        case JsonSchemaCandidate(c, jsonSchema,None, None)
      case _ => Nil
    }

    AMFValidationReport(
      conforms = results.isEmpty,
      model = "http://test.com/paylaod#validations",
      profile = profile.name, // profiles.headOption.map(_.name).getOrElse(ProfileNames.AMF)
      results = results
    )
  }

  def computeJsonSchemaCandidates(validationCandidates: Seq[ValidationCandidate]): Seq[JsonSchemaCandidate] = {
    // already caching in generated json schema annotation? how to ignore the parsed json schema annotation?
    val cache = mutable.Map[Shape, String]()
    validationCandidates.map { vc =>
      try {
        if (vc.shape.isInstanceOf[FileShape]) {
          None
        } else {
          val schemaShape: Option[String] = vc.shape match {
            case anyShape: AnyShape if anyShape.supportsInheritance =>
              generateShape(findPolymorphicShape(anyShape, vc.payload.encodes))
            case anyShape: AnyShape =>
              cache.get(vc.shape) match {
                case Some(json) => Some(json)
                case _ =>
                  val generated = generateShape(anyShape).getOrElse("")
                  cache.put(vc.shape, generated)
                  Some(generated)
              }
            case _ =>
              throw new InvalidJsonObject(
                new Exception(s"Invalid shape for json schema: ${vc.shape.getClass.getSimpleName}"))
          }

          schemaShape.map { s =>
            JsonSchemaCandidate(vc, s, loadDataNodeString(vc.payload), None)
          }
        }
      } catch {
        case e: UnknownDiscriminator => Some(JsonSchemaCandidate(vc, "", None, Some(e)))
        case e: InvalidJsonObject    => Some(JsonSchemaCandidate(vc, "", None, Some(e)))
      }
    } collect { case Some(s) => s }
  }

  // todo: move to json schema serializer? remove header and examples using comparator by json schema context
  private def generateShape(fragmentShape: Shape): Option[String] = {
    val dataType = DataTypeFragment()
    dataType.fields
      .setWithoutId(DataTypeFragmentModel.Encodes, fragmentShape) // careful, we don't want to modify the ID

    SYamlSyntaxPlugin.unparse(
      "application/json",
      SyamlParsedDocument(
        document = new JsonSchemaValidationFragmentEmitter(dataType)(JsonSchemaEmitterContext())
          .emitFragment())) // todo for logic in serializer. Hoy to handle the root?

  }

}
