package amf.shape

import amf.domain.{Annotations, DomainElement, Fields, Linkable}
import org.yaml.model.YPart

case class NilShape(fields: Fields, annotations: Annotations) extends Shape {
  override def adopted(parent: String): this.type = withId(parent + "/nil/" + name)

  override def linkCopy(): DomainElement with Linkable = NilShape().withId(id) // todo review with antonio
}

object NilShape {
  def apply(): NilShape = apply(Annotations())

  def apply(ast: YPart): NilShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NilShape = NilShape(Fields(), annotations)
}
