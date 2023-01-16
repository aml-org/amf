package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.transform.stages.elements.resolution.ElementStageTransformer
import amf.shapes.internal.domain.resolution.shape_normalization.{NormalizationContext, ReferencesFixer, ShapeInheritanceResolver}

class ShapeTransformer(context: NormalizationContext) extends ElementStageTransformer[Shape] {

  override def transform(element: Shape, configuration: AMFGraphConfiguration): Option[Shape] = {
    val resolvedInheritance = ReferencesFixer(element, context)
    Some(resolvedInheritance)
  }
}

object ShapeTransformer {
  def apply(errorHandler: AMFErrorHandler, keepEditingInfo: Boolean, profileName: ProfileName): ShapeTransformer = {
    val context = new NormalizationContext(errorHandler, keepEditingInfo, profileName)
    new ShapeTransformer(context)
  }
}
