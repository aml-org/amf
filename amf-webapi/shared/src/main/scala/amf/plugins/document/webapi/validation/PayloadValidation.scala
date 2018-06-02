package amf.plugins.document.webapi.validation

import amf.core.benchmark.ExecutionLog
import amf.core.metamodel.Type.RegExp
import amf.core.model.document.{Module, PayloadFragment}
import amf.core.model.domain._
import amf.core.parser.ParserContext
import amf.core.plugins.{AMFPayloadValidationPlugin, AMFPlugin}
import amf.core.services.RuntimeValidator
import amf.core.validation._
import amf.core.validation.core.{PropertyConstraint, ValidationProfile, ValidationSpecification}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.PayloadContext
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import org.yaml.model.{YDocument, YNode}
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class PayloadValidation(validationCandidates: Seq[ValidationCandidate],
                             validations: EffectiveValidations = EffectiveValidations())
    extends WebApiValidations {

  val profiles: ListBuffer[ValidationProfile] = ListBuffer[ValidationProfile]()

  def validate(): Future[AMFValidationReport] = {

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
    RuntimeValidator.shaclValidation(bu, finalValidations) map { report =>
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

  protected def addProfileTargets(dataNode: DataNode, shape: Shape): Unit = {
    val localProfile              = new AMFShapeValidations(shape).profile()
    val entryValidation           = localProfile.validations.head
    val entryValidationWithTarget = entryValidation.copy(targetInstance = Seq(dataNode.id))
    val restValidations           = localProfile.validations.tail
    var finalValidations          = Seq(entryValidationWithTarget) ++ restValidations
    finalValidations = processTargets(entryValidation, dataNode, finalValidations)

    profiles += localProfile.copy(validations = finalValidations)
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
  protected def processTargets(validation: ValidationSpecification,
                               node: DataNode,
                               validations: Seq[ValidationSpecification]): Seq[ValidationSpecification] = {
    var validationsAcc = validations
    // non pattern properties have precedence over pattern properties => let's sort them
    val nonPatternPropertyConstraints = validation.propertyConstraints.filter(_.patternedProperty.isEmpty)
    val patternProperties = validation.propertyConstraints.filter(_.patternedProperty.nonEmpty)
    val allProperties = nonPatternPropertyConstraints ++ patternProperties
    node match {

      case obj: ObjectNode =>
        obj.properties.foreach {
          case (propName, nodes) =>
            allProperties
              .filterNot(p => p.ramlPropertyId.startsWith(Namespace.Rdf.base))
              .find(p => p.ramlPropertyId.endsWith(s"#$propName") || matchPatternedProperty(p, propName)) match {
              case Some(propertyConstraint) if propertyConstraint.node.isDefined =>
                validationsAcc.find(v => v.id == propertyConstraint.node.get) match {
                  case Some(targetValidation) =>
                    validationsAcc = processTargets(targetValidation, nodes, validationsAcc)
                  case _ => // ignore
                }
              case _ => // ignore
            }
        }

      case array: ArrayNode =>
        validation.propertyConstraints.find(p => p.ramlPropertyId == (Namespace.Rdf + "member").iri()) match {
          case Some(memberPropertyValidation) if memberPropertyValidation.node.isDefined =>
            val itemsValidationId = memberPropertyValidation.node.get
            validationsAcc.find(v => v.id == itemsValidationId) match {
              case Some(itemsValidation) =>
                array.members.foreach { memberShape =>
                  validationsAcc = processTargets(itemsValidation, memberShape, validationsAcc)
                }
              case _ => // ignore
            }
          case _ => // ignore
        }

      case _: ScalarNode => // ignore

    }

    val newValidation = validation.copy(targetInstance = (validation.targetInstance ++ Seq(node.id)).distinct)
    validationsAcc = validationsAcc.filter(v => v.id != newValidation.id) ++ Seq(newValidation)

    validationsAcc
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
    val fragment = PayloadFragment().withMediaType(mediaType)

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
