package amf.shape

import amf.domain.{Fields, Linkable}
import amf.framework.parser.Annotations
import amf.metadata.shape.NilShapeModel
import org.yaml.model.YPart

case class NilShape(fields: Fields, annotations: Annotations) extends Shape {
  override def adopted(parent: String): this.type = withId(parent + "/nil/" + name)

  override def linkCopy(): NilShape = NilShape().withId(id) // todo review with antonio

  override def meta = NilShapeModel
}

object NilShape {
  def apply(): NilShape = apply(Annotations())

  def apply(ast: YPart): NilShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NilShape = NilShape(Fields(), annotations)
}
