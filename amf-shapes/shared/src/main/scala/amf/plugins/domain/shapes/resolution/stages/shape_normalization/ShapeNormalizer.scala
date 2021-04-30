package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.model.domain.Shape

private[stages] trait ShapeNormalizer {

  implicit val context: NormalizationContext

  def normalize(shape: Shape): Shape = {
    context.cache.get(shape.id) match {
      case Some(s) => s
      case _       => normalizeAction(shape)
    }
  }

  protected def normalizeAction(shape: Shape): Shape

}