package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape

import scala.language.implicitConversions

private[amf] class PropertyShapeMixin private[amf] (propertyShape: PropertyShape) {
  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    propertyShape.withRange(scalar)
    scalar
  }

  def withObjectRange(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    propertyShape.withRange(node)
    node
  }
}

private[amf] object DomainExtensions {
  implicit def propertyShapeToPropertyShape(property: PropertyShape): PropertyShapeMixin =
    new PropertyShapeMixin(property)
}
