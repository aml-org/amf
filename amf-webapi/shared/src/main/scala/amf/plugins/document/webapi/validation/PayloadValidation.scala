package amf.plugins.document.webapi.validation

import java.util

import amf.ProfileNames
import amf.client.plugins.{AMFPayloadValidationPlugin, AMFPlugin}
import amf.core.annotations.LexicalInformation
import amf.core.benchmark.ExecutionLog
import amf.core.emitter.RenderOptions
import amf.core.model.document.{Module, PayloadFragment}
import amf.core.model.domain._
import amf.core.parser.ParserContext
import amf.core.services.{DefaultValidationOptions, RuntimeValidator}
import amf.core.validation._
import amf.core.validation.core.{PropertyConstraint, ValidationProfile, ValidationSpecification}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.PayloadContext
import amf.plugins.document.webapi.model.DataTypeFragment
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.plugins.document.webapi.{OAS20Plugin, PayloadPlugin}
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape, SchemaShape}
import amf.plugins.syntax.SYamlSyntaxPlugin
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.github.fge.jackson.{JsonLoader, NodeType}
import com.github.fge.jsonschema.cfg.ValidationConfiguration
import com.github.fge.jsonschema.core.report.{ListProcessingReport, ListReportProvider, LogLevel, ProcessingReport}
import com.github.fge.jsonschema.format.{AbstractFormatAttribute, FormatAttribute}
import com.github.fge.jsonschema.format.draftv3.{DateAttribute, TimeAttribute}
import com.github.fge.jsonschema.format.helpers.AbstractDateFormatAttribute
import com.github.fge.jsonschema.library.DraftV4Library
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.processors.data.FullData
import com.github.fge.msgsimple.bundle.MessageBundle
import org.joda.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import org.yaml.model.{YDocument, YNode}
import org.yaml.parser.YamlParser

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExampleUnknownException(e: Throwable) extends RuntimeException(e)
class UnknownDiscriminator() extends RuntimeException
object Rfc2616Attribute {
  val instance = new Rfc2616Attribute()
}
class Rfc2616Attribute extends FormatAttribute {
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
case class PayloadValidation(validationCandidates: Seq[ValidationCandidate],
                             validations: EffectiveValidations = EffectiveValidations())
    extends WebApiValidations {

  val profiles: ListBuffer[ValidationProfile]                  = ListBuffer[ValidationProfile]()
  val validationsCache: mutable.Map[String, ValidationProfile] = mutable.Map()

  val AML_JSON_SCHEMA = "http://a.aml/amfschema#" // identifier of our custom schema

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

  def validate(): Future[AMFValidationReport] = {
    // return validateWithShacl()

    val jsonSchemaCandidates: Seq[(PayloadFragment, String, String, Option[Throwable])] = validationCandidates.map { vc =>
      try {
        val fragmentShape = vc.shape match {
          case anyShape: AnyShape if anyShape.supportsInheritance => findPolymorphicShape(anyShape, vc.payload.encodes)
          case _ => vc.shape
        }
        val dataType = DataTypeFragment().withEncodes(fragmentShape)
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
      .addFormatAttribute("RFC2616", Rfc2616Attribute.instance)
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
        profile = profiles.headOption.map(_.name).getOrElse(ProfileNames.AMF),
        results = results
      )
    }
  }

  def validateWithShacl() = {
    validationCandidates.foreach { vc =>
      val dataNode = vc.payload.encodes
      addProfileTargets(dataNode, vc.shape)
    }

    val bu = Module().withId("http://test.com/payload").withDeclares(validationCandidates.map(_.payload.encodes))

    val finalValidations = EffectiveValidations()
    profiles.foreach { p =>
      finalValidations.someEffective(p)
    }

    ExecutionLog.log(s"PayloadValidation#validate: Validating payload for ${validationCandidates.size} candidates")
    RuntimeValidator.shaclValidation(bu, finalValidations, DefaultValidationOptions) map { report =>
      val results = report.results
        .map(r => buildPayloadValidationResult(bu, r, finalValidations))
        .filter(_.isDefined)
        .map(_.get)
      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = bu.id,
        profile = profiles.head.name,
        results = results
      )
    }
  }

  def literalRepresentation(payload: PayloadFragment): Option[String] = {
    val futureText = payload.raw match {
      case Some(v) => Some(v)
      case None    =>
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
        case node: ScalarNode if node.dataType.getOrElse("") == (Namespace.Xsd + "string").iri() && text.head != '"' =>
          "\"" + text + "\""
        case _ => text
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

  protected def addProfileTargets(dataNode: DataNode, shape: Shape): Unit = {
    val localProfile              = profileForShape(shape, dataNode)
    val entryValidation           = localProfile.validations.head
    val entryValidationWithTarget = entryValidation.copy(targetInstance = Seq(dataNode.id))
    //val restValidations           = localProfile.validations.tail

//      var finalValidations          = Seq(entryValidationWithTarget) ++ restValidations

    val targetValidations = new mutable.LinkedHashMap[String, ValidationSpecification]()
    targetValidations.put(entryValidationWithTarget.id, entryValidationWithTarget)
    for (v <- localProfile.validations.tail) targetValidations.put(v.id, v)

    val finalValidations = processTargets(entryValidation, dataNode, targetValidations)

    profiles += localProfile.copy(validations = finalValidations.values.toSeq)
  }

  protected def profileForShape(shape: Shape, dataNode: DataNode): ValidationProfile = {
    if (shape.isInstanceOf[AnyShape] && shape.asInstanceOf[AnyShape].supportsInheritance) {
      new AMFShapeValidations(shape).profile(dataNode)
    } else {
      validationsCache.get(shape.id) match {
        case Some(profile) => profile
        case None => {
          val profile = new AMFShapeValidations(shape).profile(dataNode)
          validationsCache.put(shape.id, profile)
          profile
        }
      }
    }
  }

  def matchPatternedProperty(p: PropertyConstraint, propName: String): Boolean = {
    p.patternedProperty match {
      case Some(pattern) => pattern.r.findFirstIn(propName).isDefined
      case None          => false
    }
  }

  // Recursively traverse the tree of shape nodes and data nodes setting the target of the
  // shape validation to point to the matching node in the data nodes tree
  // We will also set pattern matching properties here.
  protected def processTargets(
      validation: ValidationSpecification,
      node: DataNode,
      validations: mutable.Map[String, ValidationSpecification]): mutable.Map[String, ValidationSpecification] =
    node match {
      case obj: ObjectNode  => processObjectNode(obj, validation, node, validations)
      case array: ArrayNode => processArrayNode(array, validation, node, validations)
      case _: ScalarNode    => filterValidations(validation, node, validations)
    }

  private def processObjectNode(obj: ObjectNode,
                                validation: ValidationSpecification,
                                node: DataNode,
                                validations: mutable.Map[String, ValidationSpecification]) = {
    // non pattern properties have precedence over pattern properties => let's sort them
    val (np, p) = validation.propertyConstraints
      .filterNot(p => p.ramlPropertyId startsWith Namespace.Rdf.base)
      .partition(_.patternedProperty.isEmpty)
    val allProperties = np ++ p

    var validationsAcc = validations

    for {
      (propName, nodes) <- obj.properties
      pc                <- allProperties
      if pc.ramlPropertyId.endsWith(s"#$propName") || matchPatternedProperty(pc, propName)
      itemsValidationId <- pc.node
      (id, v)           <- validationsAcc
      if id == itemsValidationId
    } {
      validationsAcc = processTargets(v, nodes, validationsAcc)
    }
    filterValidations(validation, node, validationsAcc)
  }

  private def processArrayNode(array: ArrayNode,
                               validation: ValidationSpecification,
                               node: DataNode,
                               validations: mutable.Map[String, ValidationSpecification]) = {
    var validationsAcc = validations
    for {
      pc <- validation.propertyConstraints
      if pc.ramlPropertyId == (Namespace.Rdf + "member").iri()
      itemsValidationId <- pc.node
      (id, v)           <- validationsAcc
      if id == itemsValidationId
      memberShape <- array.members
    } {
      validationsAcc = processTargets(v, memberShape, validationsAcc)
    }
    filterValidations(validation, node, validationsAcc)
  }

  private def filterValidations(validation: ValidationSpecification,
                                node: DataNode,
                                validations: mutable.Map[String, ValidationSpecification]) = {
    val newValidation = validation.withTarget(node.id)
    validations -= newValidation.id += ((newValidation.id, newValidation))
  }
}

object PayloadValidatorPlugin extends AMFPayloadValidationPlugin {

  override def canValidate(shape: Shape): Boolean = {
    shape match {
      case _: SchemaShape => false
      case _: AnyShape    => true
      case _              => false
    }
  }

  override val ID: String = "AMF Payload Validation"

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def init(): Future[AMFPlugin] = Future.successful(this)

  override val payloadMediaType: Seq[String] = Seq("application/json", "application/yaml", "text/vnd.yaml")

  val defaultCtx = new PayloadContext("", Nil, ParserContext())

  override def parsePayload(payload: String, mediaType: String): PayloadFragment = {
    val fragment = PayloadFragment(payload, mediaType)

    YamlParser(payload).parse(keepTokens = true).collectFirst({ case doc: YDocument => doc.node }) match {
      case Some(node: YNode) =>
        fragment.withEncodes(DataNodeParser(node, parent = Option(fragment.id))(defaultCtx).parse())
      case None => fragment.withEncodes(ScalarNode(payload, None))
    }
    fragment
  }

  override def validateSet(set: ValidationShapeSet): Future[AMFValidationReport] =
    PayloadValidation(set.candidates).validate()
}
