package amf.plugins.domain.webapi.resolution.stages

import amf._
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.domain.webapi.models.{Server, ServerContainer}

/**
  * Place server models in the right locations according to OAS 3.0 and our own criterium for AMF
  *
  * @param profile target profile
  */
class ServersNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean = false)
    extends TransformationStep() {

  override def apply[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
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
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        val api       = doc.encodes.asInstanceOf[Api]
        val endpoints = api.endPoints
        propagateServers(api, endpoints)
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
