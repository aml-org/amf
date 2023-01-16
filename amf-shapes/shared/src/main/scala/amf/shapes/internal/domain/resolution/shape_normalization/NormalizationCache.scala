package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import scala.collection.mutable

private[shape_normalization] case class NormalizationCache() {

  private val cache = mutable.Map[String, Shape]()

  def +(shape: Shape): this.type = {
    cache.put(shape.id, shape)
    this
  }

  def +(shape: Shape, id: String): this.type = {
    cache.put(id, shape)
    this
  }
  def get(id: String): Option[Shape] = cache.get(id)

  def exists(id: String): Boolean = cache.contains(id)
}
