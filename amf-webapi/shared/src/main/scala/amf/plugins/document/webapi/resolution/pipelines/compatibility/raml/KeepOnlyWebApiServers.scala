package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.{EndPoint, Operation}

class KeepOnlyWebApiServers()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model
        .iterator()
        .foreach({
          case operation: Operation => operation.removeServers()
          case endpoint: EndPoint   => endpoint.removeServers()

        })
      model
    } catch {
      case _: Throwable => model
    }
  }
}
