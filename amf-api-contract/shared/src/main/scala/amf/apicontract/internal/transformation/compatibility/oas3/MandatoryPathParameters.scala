package amf.apicontract.internal.transformation.compatibility.oas3

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationStep

class MandatoryPathParameters() extends TransformationStep {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    try {
      model.iterator().foreach {
        case param: Parameter if param.isPath =>
          param.withRequired(true)
        case _ =>
      }
      model
    } catch {
      case _: Exception => model
    }
  }
}
