package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.Shape

import scala.collection.mutable

private[shape_normalization] case class ResolvedInheritanceIndex() {

  private val index = mutable.Map[String, Shape]()

  def +=(shape: Shape): this.type = {
    index.put(shape.id, shape)
    this
  }

  def +=(shape: Shape, id: String): this.type = {
    index.put(id, shape)
    this
  }

  def get(id: String): Option[Shape] = index.get(id)

  def -=(id: String)              = index.remove(id)
  def exists(id: String): Boolean = index.contains(id)

}
