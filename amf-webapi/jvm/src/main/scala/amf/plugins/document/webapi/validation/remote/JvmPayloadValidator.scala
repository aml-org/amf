package amf.plugins.document.webapi.validation.remote

import amf.core.emitter.RenderOptions
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, ValidationCandidate}
import amf.internal.environment.Environment
import amf.plugins.document.webapi.OAS20Plugin
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels.DataTypeFragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.shapes.models.{AnyShape, FileShape}
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.everit.json.schema.internal.{DateFormatValidator, RegexFormatValidator, URIFormatValidator}
import org.everit.json.schema.loader.SchemaLoader
import org.everit.json.schema.Validator
import org.everit.json.schema.{Schema, ValidationException}
import org.json.{JSONObject, JSONTokener}

import scala.concurrent.Future

class JvmPayloadValidator(shape: AnyShape) extends PlatformPayloadValidator(shape) with PlatformSchemaValidator {

  val isFileShape: Boolean                = shape.isInstanceOf[FileShape]
  val polymorphic: Boolean                = shape.supportsInheritance
  var jsonSchemasMap: Map[String, Schema] = Map()
  val validator: Validator                = Validator.builder().failEarly().build()
  private val env                         = Environment()

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

  protected def shapeJsonSchema(effectiveShape: Shape): Option[Schema] = {
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
    val payloadFragment = PayloadValidatorPlugin.parsePayload(payload, mediaType, env, shape)
    val effectiveShape  = findPolymorphicShape(shape, payloadFragment.encodes)
    shapeJsonSchema(effectiveShape) match {
      case None => throw new Exception(s"Cannot parse shape '${effectiveShape.id}' to execute validation")
      case Some(jsonSchema) =>
        val dataNode = if (mediaType == "application/json") {
          loadJson(payload)
        } else {
          loadDataNodeString(payloadFragment)
        }
        try {
          validator.performValidation(jsonSchema, dataNode)
          true
        } catch {
          case _: ValidationException => false
        }
    }
  }

  protected def validateNotPolymorphic(mediaType: String, payload: String): Boolean = {
    shapeJsonSchema(shape) match {
      case None => throw new Exception(s"Cannot parse shape '${shape.id}' to execute validation")
      case Some(jsonSchema) =>
        val dataNode = if (mediaType == "application/json") {
          loadJson(payload)
        } else {
          val payloadFragment = PayloadValidatorPlugin.parsePayload(payload, mediaType, env, shape)
          loadDataNodeString(payloadFragment)
        }
        try {
          validator.performValidation(jsonSchema, dataNode)
          true
        } catch {
          case _: ValidationException => false
        }
    }
  }

  protected def parseShape(fragmentShape: Shape): Option[Schema] = {
    val dataType = DataTypeFragment()
    dataType.fields
      .setWithoutId(DataTypeFragmentModel.Encodes, fragmentShape) // careful, we don't want to modify the ID

    OAS20Plugin.unparse(dataType, RenderOptions()) match {
      case Some(doc) =>
        SYamlSyntaxPlugin.unparse("application/json", doc) match {
          case Some(jsonSchema) =>
            loadJson(jsonSchema.replace("x-amf-union", "anyOf")) match {
              case schemaNode: JSONObject =>
                schemaNode.remove("x-amf-fragmentType")
                schemaNode.remove("example")
                schemaNode.remove("examples")

                val schemaBuilder = SchemaLoader
                  .builder()
                  .schemaJson(schemaNode)
                  .addFormatValidator(DateTimeOnlyFormatValidator)
                  .addFormatValidator(Rfc2616Attribute)
                  .addFormatValidator(Rfc2616AttributeLowerCase)
                  .addFormatValidator(new DateFormatValidator())
                  .addFormatValidator(new URIFormatValidator())
                  .addFormatValidator(new RegexFormatValidator())
                  .addFormatValidator(PartialTimeFormatValidator)

                val schemaLoader = schemaBuilder.build()
                Some(schemaLoader.load().build())
              case _ => None
            }
          case _ => None
        }
      case _ =>
        None
    }
  }

  protected def loadDataNodeString(payload: PayloadFragment): Object = {
    literalRepresentation(payload) map { payloadText =>
      loadJson(payloadText)
    } match {
      case Some(parsed) => parsed
      case _            => throw new Exception("Cannot parse payload")
    }
  }

  protected def loadJson(text: String): Object = new JSONTokener(text).nextValue()

  override def validate(validationCandidates: Seq[ValidationCandidate],
                        profile: ValidationProfile): Future[AMFValidationReport] =
    throw new Exception("Validate not supported in payload validator")
}
