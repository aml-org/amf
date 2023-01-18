package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.transform.stages.elements.resolution.ElementStageTransformer
import amf.shapes.internal.domain.resolution.shape_normalization.{NormalizationContext, ShapeReferencesUpdater}

class ShapeReferencesUpdaterTransformer(context: NormalizationContext) extends ElementStageTransformer[Shape] {

  override def transform(shape: Shape, configuration: AMFGraphConfiguration): Option[Shape] = {
    val updatedShape = ShapeReferencesUpdater()(context).update(shape)
    Some(updatedShape)
  }
}

object ShapeReferencesUpdaterTransformer {
  def apply(errorHandler: AMFErrorHandler, keepEditingInfo: Boolean, profileName: ProfileName): ShapeReferencesUpdaterTransformer = {
    val context = new NormalizationContext(errorHandler, keepEditingInfo, profileName)
    new ShapeReferencesUpdaterTransformer(context)
  }
}
