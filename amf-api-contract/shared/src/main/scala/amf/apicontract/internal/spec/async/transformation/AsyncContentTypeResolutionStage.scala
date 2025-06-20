package amf.apicontract.internal.spec.async.transformation

import amf.apicontract.client.scala.model.domain.Message
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep

class AsyncContentTypeResolutionStage() extends TransformationStep() {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      val webApi = doc.encodes.asInstanceOf[Api]
      transform(webApi)
      doc
    case _ => model
  }

  private def transform(api: Api): Unit = {
    val contentType = api.contentType.headOption
    contentType.foreach { mediaType =>
      val payloads = getPayloads(api)
      payloads.filter(p => p.mediaType.option().isEmpty).foreach(_.withMediaType(mediaType.value()))
    }
  }

  private def getPayloads(api: Api) = {
    val operations      = api.endPoints.flatMap(_.operations)
    val requestPayloads = operations.flatMap(_.requests).flatMap(_.payloads)
    // added types to flatMap to avoid casting a Message to a Response to get .payloads()
    val responsePayloads = operations.flatMap { op =>
      op.responses.flatMap { m: Message =>
        m.payloads
      }
    }
    requestPayloads ++ responsePayloads
  }
}
