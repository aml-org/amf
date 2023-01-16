package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel

object ExplicitFieldAnnotationSetter {
  def apply(node: Shape): Unit = {
    node match {
      case node: NodeShape =>
        node.add(ExplicitField())

        // We make explicit the implicit fields
        node.fields.entry(NodeShapeModel.Closed) match {
          case Some(entry) =>
            node.fields.setWithoutId(
              NodeShapeModel.Closed,
              entry.value.value,
              entry.value.annotations += ExplicitField()
            )
          case None => node.set(NodeShapeModel.Closed, AmfScalar(false), Annotations() += ExplicitField())
        }
      case array: ArrayShape =>
        array.add(ExplicitField())
      case property: PropertyShape =>
        // so we don't use the '?' shortcut in raml
        property.fields.getValueAsOption(PropertyShapeModel.MinCount).map(_.annotations += ExplicitField())
      case _ =>
    }
  }
}
