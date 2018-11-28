package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage

class DefaultPayloadMediaType()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
    model
  }
}
