package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.api.{Api, AsyncApi}
import amf.apicontract.client.scala.model.domain.{EndPoint, Server}
import amf.apicontract.internal.metamodel.domain.EndPointModel
import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.UndeclaredChannelServer
import amf.core.client.common.validation.{Async20Profile, ProfileName}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.parser.domain.Annotations

/** Replaces dummy servers parsed in channels with the real ones defined in the root servers object
  *
  * @param profile
  *   target profile
  */
class ChannelServersResolutionStage(profile: ProfileName, val keepEditingInfo: Boolean = false)
    extends TransformationStep() {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    profile match {
      case Async20Profile => resolveChannelServers(model, errorHandler)
      case _              => model
    }
  }

  /** Replaces dummy servers parsed in channels with the real ones defined in the root servers object
    *
    * @param unit
    *   BaseUnit in
    * @return
    *   unit BaseUnit out
    */
  private def resolveChannelServers(unit: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[AsyncApi] =>
        val asyncApi = doc.encodes.asInstanceOf[AsyncApi]
        asyncApi.endPoints.foreach(resolveChannelServer(_, asyncApi.servers, errorHandler))
        doc
      case _ => unit
    }
  }

  private def resolveChannelServer(
      endPoint: EndPoint,
      declaredServers: Seq[Server],
      errorHandler: AMFErrorHandler
  ): Unit = {
    endPoint.fields.?[AmfArray](EndPointModel.Servers) match {
      // servers keyword not defined, channel is available in every server
      case None =>
        endPoint.setArrayWithoutId(EndPointModel.Servers, declaredServers, Annotations.synthesized())

      // server keyword declared with an empty list `servers: []`
      case Some(array: AmfArray) if array.values.isEmpty =>
        endPoint.setArrayWithoutId(EndPointModel.Servers, declaredServers, Annotations.virtual())

      // server keyword defined with a list of servers
      case Some(array: AmfArray) =>
        val endpointServers = array.values.asInstanceOf[Seq[Server]]
        val realServers     = getRealServers(endpointServers, declaredServers)

        if (realServers.nonEmpty)
          endPoint.setArrayWithoutId(EndPointModel.Servers, realServers, Annotations.inferred())

        val nonExistentServers = endpointServers.filterNot(sv => isServerDeclared(sv, declaredServers))
        nonExistentServers.foreach { sv =>
          errorHandler.violation(
            UndeclaredChannelServer,
            sv,
            s"Server '${sv.name.value()}' in channel '${endPoint.path.value()}' is not defined in the servers root object"
          )
        }
    }
  }

  private def getRealServers(endpointServers: Seq[Server], declaredServers: Seq[Server]) = {
    // up until now, the endpoint servers are dummy servers with only the name
    // we have to find the real server in the declarations
    endpointServers.flatMap(sv => declaredServers.find(s => s.name.value() == sv.name.value()))
  }

  private def isServerDeclared(sv: Server, declaredServers: Seq[Server]): Boolean =
    declaredServers.exists(s => s.name.value() == sv.name.value())
}
