package amf.shape

import amf.domain.{Fields, Linkable}
import amf.framework.parser.Annotations
import amf.metadata.shape.UnionShapeModel
import amf.metadata.shape.UnionShapeModel._
import org.yaml.model.YPart

case class UnionShape(fields: Fields, annotations: Annotations) extends Shape {

  def anyOf: Seq[Shape] = fields(AnyOf)

  def withAnyOf(elements: Seq[Shape]): this.type = this.setArray(AnyOf, elements)

  override def adopted(parent: String): this.type = withId(parent + "/union/" + name)

  override def linkCopy(): Linkable = UnionShape().withId(id)

  override def meta = UnionShapeModel
}

object UnionShape {

  def apply(): UnionShape = apply(Annotations())

  def apply(ast: YPart): UnionShape = apply(Annotations(ast))

  def apply(annotations: Annotations): UnionShape = UnionShape(Fields(), annotations)
}
