package amf.apicontract.internal.transformation.compatibility.raml

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep
import amf.shapes.client.scala.model.domain.CreativeWork

class MandatoryCreativeWorkFields() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case work: CreativeWork => fillMandatoryFields(work)
        case _                  => // ignore
      }
    } catch {
      case _: Throwable => // ignore: we don't want this to break anything
    }
    model
  }

  private def fillMandatoryFields(creativeWork: CreativeWork): Unit = {
    if (creativeWork.title.isNullOrEmpty) creativeWork.withTitle("generated")
    if (creativeWork.description.isNullOrEmpty) creativeWork.withDescription("generated")
  }
}
