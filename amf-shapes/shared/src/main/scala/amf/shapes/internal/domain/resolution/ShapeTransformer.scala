package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.transform.stages.elements.resolution.ElementStageTransformer
import amf.shapes.internal.domain.resolution.recursion.RecursionErrorRegister
import amf.shapes.internal.domain.resolution.shape_normalization.{
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
  def apply(errorHandler: AMFErrorHandler, keepEditingInfo: Boolean, profileName: ProfileName): ShapeTransformer = {
    val context = new NormalizationContext(errorHandler, keepEditingInfo, profileName)
    new ShapeTransformer(context)
  }
}
