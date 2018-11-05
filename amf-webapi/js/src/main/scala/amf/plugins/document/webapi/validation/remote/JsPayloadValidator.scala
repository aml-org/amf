package amf.plugins.document.webapi.validation.remote

import amf.core.emitter.YDocumentBuilder
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, ValidationCandidate}
import amf.internal.environment.Environment
import amf.plugins.document.webapi.Oas20Plugin
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels.DataTypeFragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.shapes.models.{AnyShape, FileShape}
import amf.plugins.syntax.SYamlSyntaxPlugin

import scala.concurrent.Future
import scala.scalajs.js

class JsPayloadValidator(shape: AnyShape) extends PlatformPayloadValidator(shape) with PlatformSchemaValidator {

  val validator: Ajv                         = AjvValidator.fast()
  val isFileShape: Boolean                   = shape.isInstanceOf[FileShape]
  val polymorphic: Boolean                   = shape.supportsInheritance
  var jsonSchemasMap: Map[String, js.Object] = Map()
  private val env                            = Environment()

  override def validate(mediaType: String, payload: String): Boolean = {
    if (mediaType != "application/json" && mediaType != "application/yaml") {
      throw new UnsupportedMediaType(
        s"Unsupported payload media type '$mediaType', only application/json and application/yaml supported")
    }
    if (isFileShape) {
      true
    } else if (polymorphic) {
      validatePolymorphic(mediaType, payload)
    } else {
      validateNotPolymorphic(mediaType, payload)
    }
  }

  protected def shapeJsonSchema(effectiveShape: Shape): Option[js.Object] = {
    jsonSchemasMap.get(effectiveShape.id) match {
      case Some(schema) => Some(schema)
      case None =>
        parseShape(effectiveShape) map { parsedShape =>
          jsonSchemasMap += (effectiveShape.id -> parsedShape)
          parsedShape
        }
    }

  }

  protected def validatePolymorphic(mediaType: String, payload: String): Boolean = {
    val payloadParsingResult = PayloadValidatorPlugin.parsePayloadWithErrorHandler(payload, mediaType, env, shape)
    if (!payloadParsingResult.hasError) {
      val payloadFragment = payloadParsingResult.fragment
      val effectiveShape  = findPolymorphicShape(shape, payloadFragment.encodes)
      shapeJsonSchema(effectiveShape) match {
        case None => throw new Exception(s"Cannot parse shape '${effectiveShape.id}' to execute validation")
        case Some(jsonSchema) =>
          val dataNode = if (mediaType == "application/json") {
            js.Dynamic.global.JSON.parse(payload)
          } else {
            loadDataNodeString(payloadFragment)
          }
          validator.validate(jsonSchema, dataNode)
      }
    } else false
  }

  protected def validateNotPolymorphic(mediaType: String, payload: String): Boolean = {
    shapeJsonSchema(shape) match {
      case None => throw new Exception(s"Cannot parse shape '${shape.id}' to execute validation")
      case Some(jsonSchema) =>
        val dataNode = if (mediaType == "application/json") {
          js.Dynamic.global.JSON.parse(payload)
        } else {
          val payloadParsingResult =
            PayloadValidatorPlugin.parsePayloadWithErrorHandler(payload, mediaType, env, shape)
          if (!payloadParsingResult.hasError) loadDataNodeString(payloadParsingResult.fragment)
          else return false
        }
        validator.validate(jsonSchema, dataNode)
    }
  }

  protected def parseShape(fragmentShape: Shape): Option[js.Object] = {
    val dataType = DataTypeFragment()
    dataType.fields
      .setWithoutId(DataTypeFragmentModel.Encodes, fragmentShape) // careful, we don't want to modify the ID

    val builder = new YDocumentBuilder
    if (!Oas20Plugin.emit(dataType, builder)) return None

    SYamlSyntaxPlugin.unparse("application/json", builder.result) match {
      case Some(jsonSchema) =>
        val schemaNode = js.Dynamic.global.JSON
          .parse(jsonSchema.toString.replace("x-amf-union", "anyOf"))
          .asInstanceOf[js.Dictionary[js.Dynamic]]
        schemaNode -= "x-amf-fragmentType"
        schemaNode -= "example"
        schemaNode -= "examples"
        Some(schemaNode.asInstanceOf[js.Object])
      case _ => None
    }
  }

  protected def loadDataNodeString(payload: PayloadFragment): js.Dynamic = {
    literalRepresentation(payload) map { payloadText => js.Dynamic.global.JSON.parse(payloadText)
    } match {
      case Some(parsed) => parsed
      case _            => throw new Exception("Cannot parse payload")
    }
  }

  override def validate(validationCandidates: Seq[ValidationCandidate],
                        profile: ValidationProfile): Future[AMFValidationReport] =
    throw new Exception("Validate not supported in payload validator")
}
