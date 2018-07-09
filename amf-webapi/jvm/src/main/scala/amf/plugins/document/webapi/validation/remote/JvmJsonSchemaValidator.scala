package amf.plugins.document.webapi.validation.remote

import java.util

import amf.core.annotations.LexicalInformation
import amf.core.emitter.RenderOptions
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DataNode, ObjectNode, ScalarNode, Shape}
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, AMFValidationResult, ValidationCandidate, _}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels.DataTypeFragmentModel
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.{OAS20Plugin, PayloadPlugin}
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}
import amf.plugins.syntax.SYamlSyntaxPlugin
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.github.fge.jackson.{JsonLoader, NodeType}
import com.github.fge.jsonschema.cfg.ValidationConfiguration
import com.github.fge.jsonschema.core.report.{ListProcessingReport, ListReportProvider, LogLevel, ProcessingReport}
import com.github.fge.jsonschema.format.FormatAttribute
import com.github.fge.jsonschema.format.draftv3.{DateAttribute, TimeAttribute}
import com.github.fge.jsonschema.format.helpers.AbstractDateFormatAttribute
import com.github.fge.jsonschema.library.DraftV4Library
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.processors.data.FullData
import com.github.fge.msgsimple.bundle.MessageBundle
import org.joda.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class UnknownDiscriminator() extends RuntimeException

object Rfc2616Attribute extends FormatAttribute {
  val name = "RFC2616"
  val pattern = "((Mon|Tue|Wed|Thu|Fri|Sat|Sun), [0-9]{2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} GMT)"

  override def supportedTypes(): util.EnumSet[NodeType] = util.EnumSet.of(NodeType.STRING)

  override def validate(report: ProcessingReport, bundle: MessageBundle, data: FullData): Unit = {
    val value = data.getInstance().getNode().textValue()

    if (!value.matches(pattern)) {
      val msg = data.newMessage()
        .put("domain", "validation")
        .put("keyword", "format")
        .setMessage("Invalid RFC2616 string")
        .putArgument("value", value).putArgument("expected", pattern)
      report.error(msg)
    }
  }
}

object DateTimeOnlyFormatAttribute extends AbstractDateFormatAttribute("date-time-only", "yyyy-MM-ddTHH:mm:ss") {
  val name = "date-time-only"

  import org.joda.time.DateTimeFieldType._

  override def getFormatter: DateTimeFormatter = {
    var builder = new DateTimeFormatterBuilder();

    builder = builder.appendFixedDecimal(year(), 4)
      .appendLiteral('-')
      .appendFixedDecimal(monthOfYear(), 2)
      .appendLiteral('-')
      .appendFixedDecimal(dayOfMonth(), 2)
      .appendLiteral('T')
      .appendFixedDecimal(hourOfDay(), 2)
      .appendLiteral(':')
      .appendFixedDecimal(minuteOfHour(), 2)
      .appendLiteral(':')
      .appendFixedDecimal(secondOfMinute(), 2)

    builder.toFormatter
  }
}

object JvmJsonSchemaValidator extends PlatformJsonSchemaValidator {

  val AML_JSON_SCHEMA = "http://a.aml/amfschema#" // identifier of our custom schema

  def validate(validationCandidates: Seq[ValidationCandidate],
               profile: ValidationProfile): Future[AMFValidationReport] = {
    // return validateWithShacl()

    val jsonSchemaCandidates: Seq[(PayloadFragment, String, String, Option[Throwable])] = validationCandidates.map { vc =>
      try {
        val fragmentShape = vc.shape match {
          case anyShape: AnyShape if anyShape.supportsInheritance => findPolymorphicShape(anyShape, vc.payload.encodes)
          case _ => vc.shape
        }
        val dataType = DataTypeFragment()
        dataType.fields.setWithoutId(DataTypeFragmentModel.Encodes, fragmentShape) // careful, we don't want to modify the ID

        OAS20Plugin.unparse(dataType, RenderOptions()) match {
          case Some(doc) =>
            SYamlSyntaxPlugin.unparse("application/json", doc) match {
              case Some(jsonSchema) =>
                Some((vc.payload, jsonSchema.replace("x-amf-union", "anyOf"), vc.shape.id, None))
              case _ =>
                None
            }
          case _ => None
        }
      } catch {
        case e: UnknownDiscriminator => Some((vc.payload, "", vc.shape.id, Some(e)))
      }
    } collect { case Some(s) => s }

    // This is a hack to add back missing formats due to a problem in the JSON-Schema library
    // See https://github.com/java-json-tools/json-schema-validator/issues/103 for an explanation
    val library = DraftV4Library.get
      .thaw
      .addFormatAttribute("date", DateAttribute.getInstance)
      .addFormatAttribute("time", TimeAttribute.getInstance)
      .addFormatAttribute("RFC2616", Rfc2616Attribute)
      .addFormatAttribute("date-time-only", DateTimeOnlyFormatAttribute)
      .freeze
    val cfg = ValidationConfiguration.newBuilder.setDefaultLibrary(AML_JSON_SCHEMA, library).freeze

    val schemaFactory = JsonSchemaFactory
      .newBuilder()
      .setValidationConfiguration(cfg)
      .setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.NONE))
      .freeze()

    val results = jsonSchemaCandidates flatMap  { case (payload, jsonSchema, shapeId, maybeException) =>

      maybeException match {
        case Some(e: UnknownDiscriminator) => Seq( // error case for polymorphic shapes without matching discriminator
          AMFValidationResult(
            message = "Unknown discriminator value",
            level = SeverityLevels.VIOLATION,
            targetNode = payload.encodes.id,
            targetProperty = None,
            validationId = (Namespace.AmfParser + "exampleError").iri(),
            position = payload.encodes.annotations.find(classOf[LexicalInformation]),
            location = None,
            source = e
          )
        )
        case Some(_)                       => Nil // we ignore other exceptions
        case None                          => // standard ase, we have a payload and shape
          loadDataNodeString(payload) match {
            case Some(dataNode) =>
              var schemaNode = loadJson(jsonSchema).asInstanceOf[com.fasterxml.jackson.databind.node.ObjectNode]
              schemaNode.remove("x-amf-fragmentType")
              schemaNode.remove("example")
              schemaNode.remove("examples")
              schemaNode = schemaNode.put("$schema", AML_JSON_SCHEMA) // this must match the one we set up
            val schema = schemaFactory.getJsonSchema(schemaNode)
              val report = schema.validate(dataNode)

              /*
              println("\n\nValidating...")
              println("  - SCHEMA:")
              println(jsonSchema)
              println("  - DATA:")
              println(dataNode)
              println(s"  ====> RESULT: ${report.isSuccess}")
              println(report.toString)
              println("-----------------------\n\n")
              */

              if (!report.isSuccess) {
                val listReport = report.asInstanceOf[ListProcessingReport].iterator()
                var resultsAcc = Seq[AMFValidationResult]()
                while (listReport.hasNext) {
                  val validationMessage = listReport.next()
                  resultsAcc = resultsAcc :+ AMFValidationResult(
                    message = validationMessage.toString,
                    level = SeverityLevels.VIOLATION,
                    targetNode = payload.encodes.id,
                    targetProperty = None,
                    validationId = (Namespace.AmfParser + "exampleError").iri(),
                    position = payload.encodes.annotations.find(classOf[LexicalInformation]),
                    location = None,
                    source = report
                  )
                }
                resultsAcc
              } else {
                Nil
              }
            case _ => Nil
          }
      }
    }

    Future {
      AMFValidationReport(
        conforms = results.isEmpty,
        model = "http://test.com/paylaod#validations",
        profile = profile.name, // profiles.headOption.map(_.name).getOrElse(ProfileNames.AMF)
        results = results
      )
    }
  }

  def findPolymorphicShape(anyShape: AnyShape, payload: DataNode): Shape = {
    val closure: Seq[Shape] = anyShape.effectiveStructuralShapes
    findPolymorphicEffectiveShape(closure, payload) match {
      case Some(shape: NodeShape) => shape
      case _ => // TODO: structurally can be a valid type, should we fail? By default RAML expects a failure
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
                  case Some(v: ScalarNode) => {
                    v.value == discriminatorValue
                  }
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
      case Some(v)  => Some(v)
      case None     =>
        val mediaType = if (payload.mediaType.option().getOrElse("application/json").contains("yaml")) "application/yaml" else "application/json"
        PayloadPlugin.unparse(payload, RenderOptions()) match {
          case Some(doc) => SYamlSyntaxPlugin.unparse(mediaType, doc) match {
            case Some(serialized) => Some(serialized)
            case _                => None
          }
          case _         => None
        }
    }

    futureText map { text =>
      payload.encodes match {
        case node: ScalarNode if node.dataType.getOrElse("") == (Namespace.Xsd + "string").iri() && text.nonEmpty && text.head != '"' =>
          "\"" + text.stripLineEnd + "\""
        case _ => text.stripLineEnd
      }
    }
  }

  protected def loadDataNodeString(payload: PayloadFragment): Option[JsonNode] = {
    try {
      literalRepresentation(payload) map { payloadText =>
        payload.mediaType.option() match {
          case Some(mt) if mt.contains("json") => loadJson(payloadText)
          case Some(mt) if mt.contains("yaml") => loadYaml(payloadText)
          case _ => {
            try {
              JsonLoader.fromString(payloadText)
            } catch {
              case _: Exception =>
                new YAMLMapper().readTree(payloadText)
            }
          }
        }
      }
    } catch {
      case _: ExampleUnknownException => None
    }
  }

  protected def loadJson(text: String): JsonNode = {
    try {
      JsonLoader.fromString(text)
    } catch {
      case _: Throwable => loadYaml(text) // some strings detected as JSON are invalid JSON but valid YAML
    }
  }

  protected def loadYaml(text: String): JsonNode = {
    try {
      new YAMLMapper().readTree(text)
    } catch {
      case e: Throwable => throw new ExampleUnknownException(e)
    }
  }

}
