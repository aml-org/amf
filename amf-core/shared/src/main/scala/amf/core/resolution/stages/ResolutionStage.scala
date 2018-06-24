package amf.core.resolution.stages

import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler

abstract class ResolutionStage()(implicit val errorHandler: ErrorHandler) {
  def resolve[T <: BaseUnit](model: T): T
}
