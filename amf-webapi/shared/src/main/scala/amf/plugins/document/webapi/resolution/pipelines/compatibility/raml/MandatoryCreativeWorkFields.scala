package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.models.WebApi

class MandatoryCreativeWorkFields()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
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
