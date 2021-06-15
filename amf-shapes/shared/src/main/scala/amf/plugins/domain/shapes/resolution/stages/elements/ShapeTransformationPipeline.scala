package amf.plugins.domain.shapes.resolution.stages.elements

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.transform.pipelines.elements.ElementTransformationPipeline
import amf.core.client.scala.transform.stages.elements.resolution.ElementStageTransformer
import amf.plugins.domain.shapes.resolution.stages.{ShapeChainLinksTransformer, ShapeTransformer}

class CompleteShapeTransformationPipeline(shape: Shape, errorHandler: AMFErrorHandler, profileName: ProfileName)
    extends ElementTransformationPipeline[Shape](shape, errorHandler: AMFErrorHandler) {
  override val steps: Seq[ElementStageTransformer[Shape]] = Seq(
    new ShapeChainLinksTransformer(),
    ShapeTransformer(errorHandler, keepEditingInfo = false, profileName)
  )
}
