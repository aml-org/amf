package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.NilShapeModel
import org.yaml.model.YPart

case class NilShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {
  override def adopted(parent: String): this.type = withId(parent + "/nil/" + name.value())

  override def linkCopy(): NilShape = NilShape().withId(id) // todo review with antonio

  override def meta: Obj = NilShapeModel
}

object NilShape {
  def apply(): NilShape = apply(Annotations())

  def apply(ast: YPart): NilShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NilShape = NilShape(Fields(), annotations)
}
