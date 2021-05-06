package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.models.{AnyShape, ArrayShape}

class AddItemsToArrayType() extends TransformationStep {
  override def transform[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
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
