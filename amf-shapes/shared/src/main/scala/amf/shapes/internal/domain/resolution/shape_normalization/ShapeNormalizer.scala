package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import scala.collection.mutable

private[resolution] trait ShapeNormalizer {

  protected def normalize(shape: Shape): Shape

  protected def normalizeAction(shape: Shape): Shape

}
