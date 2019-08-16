package amf.plugins.domain.shapes.resolution.stages.elements

import amf.ProfileName
import amf.core.model.domain.Shape
import amf.core.parser.ErrorHandler
import amf.core.resolution.pipelines.elements.ElementTransformationPipeline
import amf.core.resolution.stages.elements.resolution.ElementStageTransformer
import amf.plugins.domain.shapes.resolution.stages.ShapeTransformer

class ShapeTransformationPipeline(shape: Shape, errorHandler: ErrorHandler, profileName: ProfileName)
    extends ElementTransformationPipeline[Shape](shape, errorHandler: ErrorHandler) {
  override val steps: Seq[ElementStageTransformer[Shape]] = Seq(
    ShapeTransformer(errorHandler, keepEditingInfo = false, profileName)
  )
}
