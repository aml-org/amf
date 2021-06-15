package amf.plugins.document.apicontract.resolution.pipelines.compatibility.oas3

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep

class AddItemsToArrayType() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model
        .iterator()
        .foreach({
          case array: ArrayShape => setArrayType(array)
          case _                 =>
        })
      model
    } catch {
      case _: Throwable => model
    }
  }

  private def setArrayType(array: ArrayShape): Unit = {
    Option(array.items) match {
      case None => array.withItems(AnyShape())
      case _    => // ignore
    }
  }
}
