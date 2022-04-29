package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.Field

class SecurityResolutionStage() extends TransformationStep() {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        resolveSecurity(doc.encodes.asInstanceOf[Api])
      case _ =>
    }
    model
  }

  protected def resolveSecurity(api: Api): Unit = {
    val rootSecurity = getAndRemove(api, BaseApiModel.Security)

    api.endPoints.foreach { endPoint =>
      val endPointSecurity = overrideWith(rootSecurity, getAndRemove(endPoint, EndPointModel.Security))

      endPoint.operations.foreach { operation =>
        // I need to know if this is an empty array or if it's not defined.
        val opSecurity = getAndRemove(operation, OperationModel.Security)

        overrideWith(endPointSecurity, opSecurity).foreach { requirements =>
          if (requirements.nonEmpty) operation.setArray(OperationModel.Security, requirements)
        }
      }
    }
  }

  private def getAndRemove(element: DomainElement, field: Field): Option[Seq[SecurityRequirement]] = {
    val result = element.fields.entry(field).map(_.array.values.map(v => v.asInstanceOf[SecurityRequirement]))
    element.fields.removeField(field)
    result
  }

  private def overrideWith(
      base: Option[Seq[SecurityRequirement]],
      overrider: Option[Seq[SecurityRequirement]]
  ): Option[Seq[SecurityRequirement]] =
    overrider.orElse(base).filter(_.nonEmpty)
}
