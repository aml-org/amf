package amf.plugins.domain.apicontract.resolution.stages.async

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.stages.TransformationStep
import amf.plugins.domain.apicontract.models.api.Api

class AsyncContentTypeResolutionStage() extends TransformationStep() {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      val webApi = doc.encodes.asInstanceOf[Api]
      resolve(webApi)
      doc
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
