package amf.plugins.document.webapi.validation

import amf.core.model.document.PayloadFragment
import amf.core.model.domain._
import amf.core.parser.ParserContext
import amf.core.plugins.{AMFPayloadValidationPlugin, AMFPlugin}
import amf.core.remote.{Raml, Vendor}
import amf.core.services.RuntimeValidator
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.{AMFValidationReport, EffectiveValidations, SeverityLevels}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.{PayloadContext, Raml10VersionFactory, SpecVersionFactory, WebApiContext}
import amf.plugins.document.webapi.parser.spec.{SpecSyntax, WebApiDeclarations}
import amf.plugins.document.webapi.parser.spec.common.DataNodeParser
import amf.plugins.document.webapi.parser.spec.raml.Raml10Syntax
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape}
import org.yaml.model.{YDocument, YNode}
import org.yaml.parser.YamlParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PayloadValidation {
  def apply(shape: Shape): PayloadValidation = new PayloadValidation(shape)
}

case class PayloadValidation(shape: Shape) extends WebApiValidations {
  var profile = Some(new AMFShapeValidations(shape).profile())

  def validate(payloadFragment: PayloadFragment): Future[AMFValidationReport] = {

    val dataNode = payloadFragment.encodes
    addProfileTargets(dataNode)
    val validations = EffectiveValidations().someEffective(profile.get)
    RuntimeValidator.shaclValidation(payloadFragment, validations) map { report =>
      val results = report.results
        .map(r => buildPayloadValidationResult(payloadFragment, r, validations))
        .filter(_.isDefined)
        .map(_.get)
      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = payloadFragment.id,
        profile = profile.get.name,
        results = results
      )
    }
  }

  protected def addProfileTargets(dataNode: DataNode): Unit = {
    val entryValidation           = profile.get.validations.head
    val entryValidationWithTarget = entryValidation.copy(targetInstance = Seq(dataNode.id))
    val restValidations           = profile.get.validations.tail
    var finalValidations          = Seq(entryValidationWithTarget) ++ restValidations
    finalValidations = processTargets(entryValidation, dataNode, finalValidations)

    profile = Some(profile.get.copy(validations = finalValidations))
  }

  protected def processTargets(validation: ValidationSpecification,
                               node: DataNode,
                               validations: Seq[ValidationSpecification]): Seq[ValidationSpecification] = {
    var validationsAcc = validations
    node match {

      case obj: ObjectNode =>
        obj.properties.foreach {
          case (propName, nodes) =>
            validation.propertyConstraints.find(p => p.ramlPropertyId.endsWith(s"#$propName")) match {
              case Some(propertyConstraint) if propertyConstraint.node.isDefined =>
                validations.find(v => v.id == propertyConstraint.node.get) match {
                  case Some(targetValidation) =>
                    validationsAcc = processTargets(targetValidation, nodes, validationsAcc)
                  case _ => // ignore
                }
              case None => // ignore
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

  override def validatePayload(shape: Shape, payloadFragment: PayloadFragment): Future[AMFValidationReport] =
    PayloadValidation(shape).validate(payloadFragment)

  override val payloadMediaType: Seq[String] = Seq("application/json", "application/yaml", "text/vnd.yaml")

  val defaultCtx = new PayloadContext(ParserContext())

  override def validatePayload(shape: Shape, payload: String, mediaType: String): Future[AMFValidationReport] = {
    val fragment = PayloadFragment().withMediaType(mediaType)

    YamlParser(payload).parse(keepTokens = true).collectFirst({ case doc: YDocument => doc.node }) match {
      case Some(node: YNode) =>
        fragment.withEncodes(DataNodeParser(node, parent = Option(fragment.id))(defaultCtx).parse())
      case None => fragment.withEncodes(ScalarNode(payload, None))
    }
    validatePayload(shape, fragment)

  }

}
