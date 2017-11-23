package amf.model

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class PropertyShape(private[amf] val propertyShape: models.PropertyShape) {

  val path: String = propertyShape.path
  val range: Shape = wrapShape(propertyShape.range)

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

  def withObjectRange(name: String): NodeShape = {
    NodeShape(propertyShape.withObjectRange(name))
  }

  def withScalarSchema(name: String): ScalarShape = {
    ScalarShape(propertyShape.withScalarSchema(name))
  }

  private def wrapShape(shape: models.Shape): Shape =
    (shape match {
      case node: models.NodeShape     => Some(NodeShape(node))
      case scalar: models.ScalarShape => Some(ScalarShape(scalar))
      case a                             => None
    }).orNull

}
