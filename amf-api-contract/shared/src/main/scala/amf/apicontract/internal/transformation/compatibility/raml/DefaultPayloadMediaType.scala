package amf.apicontract.internal.transformation.compatibility.raml

import amf.apicontract.client.scala.model.domain.Payload
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep

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
