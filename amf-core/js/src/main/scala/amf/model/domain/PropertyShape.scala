package amf.model.domain

import amf.core.model.domain.extensions

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class PropertyShape(private[amf] val propertyShape: extensions.PropertyShape) extends DomainElement {

  def path: String = propertyShape.path
  def range: Shape = platform.wrap(propertyShape.range)

  def minCount: Int = propertyShape.minCount
  def maxCount: Int = propertyShape.maxCount

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

}