package amf.model.domain

import amf.core.model.domain.extensions

case class PropertyShape(private[amf] val propertyShape: extensions.PropertyShape) extends DomainElement {

  val path: String = propertyShape.path
  val range: Shape = platform.wrap(propertyShape.range)

  val minCount: Int = propertyShape.minCount
  val maxCount: Int = propertyShape.maxCount

  def withPath(path: String): this.type = {
    propertyShape.withPath(path)
    this
  }

  def withRange(range: Shape): this.type = {
    propertyShape.withRange(range.shape)
    this
  }

  def withMinCount(min: Int): this.type = {
    propertyShape.withMinCount(min)
    this
  }
  def withMaxCount(max: Int): this.type = {
    propertyShape.withMaxCount(max)
    this
  }

  override def element: extensions.PropertyShape = propertyShape

  /* @modularization
  def withObjectRange(name: String): NodeShape = {
    NodeShape(propertyShape.withObjectRange(name))
  }

  def withScalarSchema(name: String): ScalarShape = {
    ScalarShape(propertyShape.withScalarSchema(name))
  }
  */
}