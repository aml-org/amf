package amf.plugins.domain.shapes.models

import amf.domain.Fields
import amf.framework.parser.Annotations
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import org.yaml.model.YPart

case class AnyShape(fields: Fields, annotations: Annotations) extends Shape {
  override def adopted(parent: String): this.type = withId(parent + "/any/" + name)

  override def linkCopy(): AnyShape = AnyShape().withId(id)

  override def meta = AnyShapeModel
}

object AnyShape {
  def apply(): AnyShape = apply(Annotations())

  def apply(ast: YPart): AnyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): AnyShape = AnyShape(Fields(), annotations)
}
