package amf.plugins.document.apicontract.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.models.CreativeWork

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
