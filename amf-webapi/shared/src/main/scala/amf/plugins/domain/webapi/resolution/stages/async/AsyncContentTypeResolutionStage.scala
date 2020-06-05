package amf.plugins.domain.webapi.resolution.stages.async

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.WebApi

class AsyncContentTypeResolutionStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {
  override def resolve[T <: BaseUnit](model: T): T = model match {
    case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
      val webApi = doc.encodes.asInstanceOf[WebApi]
      resolve(webApi)
      doc.asInstanceOf[T]
    case _ => model
  }

  private def resolve(webApi: WebApi): Unit = {
    val contentType = webApi.contentType.headOption
    contentType.foreach { mediaType =>
      val payloads = getPayloads(webApi)
      payloads.filter(p => p.mediaType.option().isEmpty).foreach(_.withMediaType(mediaType.value()))
    }
  }

  private def getPayloads(webApi: WebApi) = {
    val operations = webApi.endPoints.flatMap(_.operations)
    operations.flatMap(_.requests).flatMap(_.payloads) ++ operations.flatMap(_.responses).flatMap(_.payloads)
  }
}
