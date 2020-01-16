package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.{AnyShape, ArrayShape}

class AddItemsToArrayType()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
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
