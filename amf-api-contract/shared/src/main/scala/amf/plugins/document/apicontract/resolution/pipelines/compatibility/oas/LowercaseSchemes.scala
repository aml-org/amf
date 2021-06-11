package amf.plugins.document.apicontract.resolution.pipelines.compatibility.oas

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.metamodel.Field
import amf.plugins.domain.apicontract.metamodel.OperationModel
import amf.plugins.domain.apicontract.metamodel.api.BaseApiModel
import amf.plugins.domain.apicontract.models.Operation
import amf.plugins.domain.apicontract.models.api.Api

import scala.language.postfixOps

class LowercaseSchemes() extends TransformationStep {

  private def capitalizeProtocols(element: DomainElement, schemes: Seq[StrField], field: Field) = {
    val valid = Seq("HTTP", "HTTPS")
    val s =
      schemes.flatMap(_.option()).filter(scheme => valid.exists(scheme.equalsIgnoreCase)).map(_.toLowerCase)
    element.fields.removeField(field)
    if (s.nonEmpty) element.set(field, s)
  }

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case d: Document if d.encodes.isInstanceOf[Api] =>
      try {
        val api = d.encodes.asInstanceOf[Api]
        capitalizeProtocols(api, api.schemes, BaseApiModel.Schemes)

        model.iterator().foreach {
          case op: Operation =>
            capitalizeProtocols(op, op.schemes, OperationModel.Schemes)
          case _ => // ignore
        }
      } catch {
        case _: Throwable => // ignore: we don't want this to break anything
      }
      model
    case _ => model
  }
}
