package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.Payload

class DefaultPayloadMediaType() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case payload: Payload if payload.mediaType.isNullOrEmpty =>
          payload.withMediaType("*/*")
        case _ => // ignore
      }
    } catch {
      case e: Throwable => // ignore: we don't want this to break anything
    }
    model
  }
}
