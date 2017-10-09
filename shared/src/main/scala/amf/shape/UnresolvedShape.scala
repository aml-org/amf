package amf.shape

import amf.domain.{Annotations, Fields, Linkable}
import org.yaml.model.YPart

/**
  * Unresolved shape: intended to be resolved after parsing (exception is thrown if shape is not resolved).
  */
case class UnresolvedShape(fields: Fields, annotations: Annotations) extends Shape {

  override def linkCopy(): Linkable = this

  override def adopted(parent: String): this.type = this
}

object UnresolvedShape {

  def apply(): NodeShape = apply(Annotations())

  def apply(ast: YPart): NodeShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NodeShape = NodeShape(Fields(), annotations)
}
