package amf.plugins.domain.shapes.resolution.stages.elements

import amf.ProfileName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.domain.Shape
import amf.core.resolution.pipelines.elements.ElementTransformationPipeline
import amf.core.resolution.stages.elements.resolution.ElementStageTransformer
import amf.plugins.domain.shapes.resolution.stages.{
  ShapeChainLinksTransformer,
  ShapeLinksTransformer,
  ShapeTransformer
}

class CompleteShapeTransformationPipeline(shape: Shape, errorHandler: AMFErrorHandler, profileName: ProfileName)
    extends ElementTransformationPipeline[Shape](shape, errorHandler: AMFErrorHandler) {
  override val steps: Seq[ElementStageTransformer[Shape]] = Seq(
    new ShapeChainLinksTransformer(),
    ShapeTransformer(errorHandler, keepEditingInfo = false, profileName)
  )
}
