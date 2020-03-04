package amf.plugins.domain.webapi.resolution.stages

import amf._
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.{Server, ServerContainer, WebApi}

/**
  * Place server models in the right locations according to OAS 3.0 and our own criterium for AMF
  *
  * @param profile target profile
  */
class ServersNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean = false)(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = {
    profile match {
      // TODO should run for Amf too
      case Oas30Profile => normalizeServers(model).asInstanceOf[T]
      case _            => model
    }
  }

  /**
    * Push all server definitions to the operation level.
    *
    * @param unit BaseUnit in
    * @return unit BaseUnit out
    */
  protected def normalizeServers(unit: BaseUnit): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        val webApi    = doc.encodes.asInstanceOf[WebApi]
        val endpoints = webApi.endPoints
        propagateServers(webApi, endpoints)
        endpoints.foreach { endPoint =>
          propagateServers(endPoint, endPoint.operations)
        }
        doc
      case _ => unit
    }
  }

  /**
    * moves servers defined in base to each child that has no servers defined.
    */
  private def propagateServers(base: ServerContainer, children: Seq[ServerContainer]): Unit =
    if (children.nonEmpty && base.servers.nonEmpty) {
      val servers: Seq[Server] = base.servers
      if (!keepEditingInfo) base.removeServers()
      children.foreach { child =>
        if (child.servers.isEmpty)
          child.withServers(servers)
      }
    }

}
