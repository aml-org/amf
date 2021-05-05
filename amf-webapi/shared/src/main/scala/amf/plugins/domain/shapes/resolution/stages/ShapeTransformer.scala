package amf.plugins.domain.shapes.resolution.stages

import amf.ProfileName
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.Shape
import amf.core.resolution.stages.elements.resolution.ElementStageTransformer
import amf.plugins.domain.shapes.resolution.stages.recursion.RecursionErrorRegister
import amf.plugins.domain.shapes.resolution.stages.shape_normalization.{
  NormalizationContext,
  ShapeCanonizer,
  ShapeExpander
}

class ShapeTransformer(context: NormalizationContext) extends ElementStageTransformer[Shape] {

  private val recursionRegister = new RecursionErrorRegister(context.errorHandler)
  override def transform(element: Shape): Option[Shape] = {
    val expanded  = ShapeExpander(element, context, recursionRegister)
    val canonized = ShapeCanonizer(expanded, context)
    Some(canonized)

  }
}

object ShapeTransformer {
  def apply(errorHandler: ErrorHandler, keepEditingInfo: Boolean, profileName: ProfileName): ShapeTransformer = {
    val context = new NormalizationContext(errorHandler, keepEditingInfo, profileName)
    new ShapeTransformer(context)
  }
}
