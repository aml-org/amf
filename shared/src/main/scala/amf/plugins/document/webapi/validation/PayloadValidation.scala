package amf.plugins.document.webapi.validation

import amf.document.BaseUnit
import amf.document.Fragment.Fragment
import amf.domain.extensions.{ArrayNode, DataNode, ObjectNode, ScalarNode}
import amf.domain.Fields
import amf.framework.parser.Annotations
import amf.framework.services.RuntimeValidator
import amf.framework.validation.{AMFValidationReport, AMFValidationResult, EffectiveValidations, SeverityLevels}
import amf.metadata.document.FragmentModel
import amf.remote.Platform
import amf.shape.Shape
import amf.validation.model.{AMFShapeValidations, ValidationSpecification}
import amf.vocabulary.Namespace

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PayloadValidation {
  def apply(platform: Platform, shape: Shape): PayloadValidation = new PayloadValidation(platform, shape)
}

case class PayloadFragment(encoded: DataNode, fields: Fields = Fields(), annotations: Annotations = Annotations())
  extends Fragment {
  fields.setWithoutId(FragmentModel.Encodes, encoded)
  override def encodes: DataNode = encoded
}

class PayloadValidation(platform: Platform, shape: Shape) extends WebApiValidations {

  var profile = Some(new AMFShapeValidations(shape).profile())

  def validate(dataNode: DataNode): Future[AMFValidationReport] = {

    addProfileTargets(dataNode)
    val validations = EffectiveValidations().someEffective(profile.get)
    val baseUnit = model(dataNode)
    RuntimeValidator.shaclValidation(baseUnit, validations) map { report =>
      val results = report.results.map(r => buildPayloadValidationResult(baseUnit, r, validations))
        .filter(_.isDefined)
        .map(_.get)
      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = baseUnit.id,
        profile = profile.get.name,
        results = results
      )
    }
  }

  protected def model(dataNode: DataNode): BaseUnit = {
    val doc = PayloadFragment(dataNode, Fields(), Annotations())
    doc.withLocation("http://test.com/payload")
    doc.withId("http://test.com/payload")
    doc
  }

  protected def addProfileTargets(dataNode: DataNode): Unit = {
    val entryValidation           = profile.get.validations.head
    val entryValdiationWithTarget = entryValidation.copy(targetInstance = Seq(dataNode.id))
    val restValidations           = profile.get.validations.tail
    var finalValidations          = Seq(entryValdiationWithTarget) ++ restValidations
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