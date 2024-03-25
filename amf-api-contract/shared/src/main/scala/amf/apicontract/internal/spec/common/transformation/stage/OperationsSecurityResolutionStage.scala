package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.apicontract.client.scala.model.domain.{Operation, Server}
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.core.client.common.validation.{Async20Profile, ProfileName}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.parser.domain.Annotations

/** Places all available security schemes in all operations unless an operation defines a subset of those */
class OperationsSecurityResolutionStage(profile: ProfileName, val keepEditingInfo: Boolean = false)
    extends TransformationStep() {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    profile match {
      case Async20Profile => resolveOperationSecuritySchemas(model)
      case _              => model
    }
  }

  private def resolveOperationSecuritySchemas(unit: BaseUnit): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[AsyncApi] =>
        val asyncApi = doc.encodes.asInstanceOf[AsyncApi]
        asyncApi.endPoints.foreach { endpoint =>
          endpoint.operations.foreach(resolveOperationSecurity(_, endpoint.servers))
        }
        doc
      case _ => unit
    }
  }

  private def resolveOperationSecurity(
      operation: Operation,
      endpointServers: Seq[Server]
  ): Unit = {
    val securities = endpointServers.flatMap(_.security)
    operation.fields.?[AmfArray](OperationModel.Security) match {
      // security keyword not defined, channel has every security that's available in every server it applies
      case None =>
        if (securities.nonEmpty)
          operation.setArrayWithoutId(OperationModel.Security, securities, Annotations.synthesized())

      // security keyword declared with an empty list `servers: []`, same as above
      case Some(array: AmfArray) if array.values.isEmpty =>
        if (securities.nonEmpty)
          operation.setArrayWithoutId(OperationModel.Security, securities, Annotations.synthesized())

      // security keyword defined with a list of security schemas from parsing, do nothing
      case _ => // ignore
    }
  }
}
