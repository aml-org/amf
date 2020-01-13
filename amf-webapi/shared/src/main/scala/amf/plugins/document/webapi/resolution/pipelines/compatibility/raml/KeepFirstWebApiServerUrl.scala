package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.{EndPoint, Operation, WebApi}

class KeepFirstWebApiServerUrl()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  private def keepFirstExistingServer(webapi: WebApi) = {
    val serverOption = webapi.servers.headOption
    webapi.removeServers()
    serverOption match {
      case Some(server) => webapi.withServer(server.url.value())
      case None         =>
    }
  }

  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model
        .iterator()
        .foreach({
          case webapi: WebApi => keepFirstExistingServer(webapi)
        })
      model
    } catch {
      case _: Throwable => model
    }
  }
}
