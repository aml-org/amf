package amf.plugins.domain.webapi.resolution.stages.async

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.api.Api

class AsyncContentTypeResolutionStage() extends TransformationStep() {
  override def transform[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      val webApi = doc.encodes.asInstanceOf[Api]
      resolve(webApi)
      doc.asInstanceOf[T]
    case _ => model
  }

  private def resolve(api: Api): Unit = {
    val contentType = api.contentType.headOption
    contentType.foreach { mediaType =>
      val payloads = getPayloads(api)
      payloads.filter(p => p.mediaType.option().isEmpty).foreach(_.withMediaType(mediaType.value()))
    }
  }

  private def getPayloads(api: Api) = {
    val operations = api.endPoints.flatMap(_.operations)
    operations.flatMap(_.requests).flatMap(_.payloads) ++ operations.flatMap(_.responses).flatMap(_.payloads)
  }
}
