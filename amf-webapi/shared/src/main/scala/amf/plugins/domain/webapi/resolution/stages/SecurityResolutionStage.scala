package amf.plugins.domain.webapi.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.DomainElement
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.metamodel.api.BaseApiModel
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel}
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.domain.webapi.models.security.SecurityRequirement

class SecurityResolutionStage() extends TransformationStep() {

  override def apply[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        resolveSecurity(doc.encodes.asInstanceOf[Api])
      case _ =>
    }
    model.asInstanceOf[T]
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

  private def overrideWith(base: Option[Seq[SecurityRequirement]],
                           overrider: Option[Seq[SecurityRequirement]]): Option[Seq[SecurityRequirement]] =
    overrider.orElse(base).filter(_.nonEmpty)
}
