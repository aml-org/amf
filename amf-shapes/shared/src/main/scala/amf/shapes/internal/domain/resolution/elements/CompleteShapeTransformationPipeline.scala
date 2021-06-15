package amf.shapes.internal.domain.resolution.elements

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.transform.pipelines.elements.ElementTransformationPipeline
import amf.core.client.scala.transform.stages.elements.resolution.ElementStageTransformer
import amf.shapes.internal.domain.resolution.{ShapeChainLinksTransformer, ShapeTransformer}

class CompleteShapeTransformationPipeline(shape: Shape, errorHandler: AMFErrorHandler, profileName: ProfileName)
    extends ElementTransformationPipeline[Shape](shape, errorHandler: AMFErrorHandler) {
  override val steps: Seq[ElementStageTransformer[Shape]] = Seq(
    new ShapeChainLinksTransformer(),
    ShapeTransformer(errorHandler, keepEditingInfo = false, profileName)
  )
}
