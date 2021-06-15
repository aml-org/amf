package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.Shape

private[resolution] trait ShapeNormalizer {

  implicit val context: NormalizationContext

  def normalize(shape: Shape): Shape = {
    context.cache.get(shape.id) match {
      case Some(s) => s
      case _       => normalizeAction(shape)
    }
  }

  protected def normalizeAction(shape: Shape): Shape

}
