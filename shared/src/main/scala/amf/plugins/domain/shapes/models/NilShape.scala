package amf.plugins.domain.shapes.models

import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.NilShapeModel
import org.yaml.model.YPart

case class NilShape(fields: Fields, annotations: Annotations) extends AnyShape {
  override def adopted(parent: String): this.type = withId(parent + "/nil/" + name)

  override def linkCopy(): NilShape = NilShape().withId(id) // todo review with antonio

  override def meta = NilShapeModel
}

object NilShape {
  def apply(): NilShape = apply(Annotations())

  def apply(ast: YPart): NilShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NilShape = NilShape(Fields(), annotations)
}
