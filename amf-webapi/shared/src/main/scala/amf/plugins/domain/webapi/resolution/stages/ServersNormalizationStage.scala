package amf.plugins.domain.webapi.resolution.stages

import amf._
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.DomainElement
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.models.{Server, WebApi}

/**
  * Place server models in the right locations according to OAS 3.0 and our own criterium for AMF
  *
  * @param profile target profile
  */
class ServersNormalizationStage(profile: ProfileName)(override implicit val errorHandler: ErrorHandler)
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
  private def propagateServers(base: DomainElement, children: Seq[DomainElement]): Unit =
    if (children.nonEmpty) {
      val servers: Seq[Server] = base.fields.field(WebApiModel.Servers)
      base.fields.removeField(WebApiModel.Servers)
      children.foreach { child =>
        if (!child.fields.exists(WebApiModel.Servers))
          child.setArray(WebApiModel.Servers, servers)
      }
    }

}
