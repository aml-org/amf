package amf.shape

import amf.domain.{Annotations, DomainElement, Fields, Linkable}
import org.yaml.model.YPart

case class AnyShape(fields: Fields, annotations: Annotations) extends Shape {
  override def adopted(parent: String): this.type = withId(parent + "/any/" + name)

  override def linkCopy(): AnyShape = AnyShape().withId(id)
}

object AnyShape {
  def apply(): AnyShape = apply(Annotations())

  def apply(ast: YPart): AnyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): AnyShape = AnyShape(Fields(), annotations)
}
