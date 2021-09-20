package amf.apicontract.internal.transformation.compatibility.oas3

import amf.apicontract.client.scala.model.domain.{Operation, Response}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationStep

class MandatoryResponses() extends TransformationStep {

  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
    try {
      model.iterator().foreach {
        case operation: Operation =>
          if (operation.responses.isEmpty) {
            operation.withResponses(Seq(Response().withName("200").withStatusCode("200").withDescription("")))
          }
        case _ =>
      }
      model
    } catch {
      case _: Exception => model
    }
  }

}
