package amf.apicontract.internal.transformation.compatibility.oas

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.metamodel.Field

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
