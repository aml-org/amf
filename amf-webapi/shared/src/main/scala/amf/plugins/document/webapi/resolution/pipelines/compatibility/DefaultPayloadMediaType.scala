package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload

class DefaultPayloadMediaType()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model.findByType(PayloadModel.`type`.head.iri()).foreach {
        case payload: Payload if payload.mediaType.isNullOrEmpty =>
          payload.withMediaType("*/*")
        case _                => // ignore
      }
    } catch {
      case e: Throwable => // ignore: we don't want this to break anything
    }
    model
  }
}
