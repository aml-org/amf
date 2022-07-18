package amf.graphqlfederation.internal.spec.context.linking.fieldset

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.Annotations

case class PropertyShapePathExpression(root: Shape, expressionComponents: Seq[PropertyShapePathExpression.Component]) {
  def +(name: String, annotations: Annotations): PropertyShapePathExpression = {
    val component = PropertyShapePathExpression.Component(name, annotations)
    PropertyShapePathExpression(root, expressionComponents :+ component)
  }
}

object PropertyShapePathExpression {
  case class Component(name: String, annotations: Annotations)
}
