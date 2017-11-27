package amf.plugins.domain.shapes.models

import amf.core.model.domain.extensions.PropertyShape

class PropertyShapeMixin(propertyShape: PropertyShape) {
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

object DomainExtensions {
  implicit def propertyShapeToPropertyShape(property: PropertyShape) = new PropertyShapeMixin(property)
}
