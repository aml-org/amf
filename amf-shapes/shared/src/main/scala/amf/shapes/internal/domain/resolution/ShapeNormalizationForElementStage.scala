package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.transform.stages.elements.resolution.ElementStageTransformer
import amf.shapes.internal.domain.resolution.shape_normalization.{
  NormalizationContext,
  ShapeInheritanceResolver,
  ShapeReferencesUpdater
}

class ShapeNormalizationForElementStage(context: NormalizationContext) extends ElementStageTransformer[Shape] {

  override def transform(shape: Shape, configuration: AMFGraphConfiguration): Option[Shape] = {
    val resolvedInheritance = ShapeInheritanceResolver()(context).normalize(shape)
    val fixedReferences     = ShapeReferencesUpdater()(context).update(resolvedInheritance)
    Some(fixedReferences)
  }
}

object ShapeNormalizationForElementStage {
  def apply(
      errorHandler: AMFErrorHandler,
      keepEditingInfo: Boolean,
      profileName: ProfileName
  ): ShapeNormalizationForElementStage = {
    val context = new NormalizationContext(errorHandler, keepEditingInfo, profileName)
    new ShapeNormalizationForElementStage(context)
  }
}
