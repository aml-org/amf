package amf.shape

import amf.domain.{Annotations, Fields, Linkable}
import org.yaml.model.YPart

/**
  * Unresolved shape: intended to be resolved after parsing (exception is thrown if shape is not resolved).
  */
case class UnresolvedShape(fields: Fields, annotations: Annotations, reference: String) extends Shape {

  override def linkCopy(): Linkable = this

  override def adopted(parent: String): this.type = this

  /** Resolve [[UnresolvedShape]] as link to specified target. */
  def resolve(target: Shape): Shape = target.link(reference, annotations).asInstanceOf[Shape].withName(name)
}

object UnresolvedShape {
  def apply(reference: String): UnresolvedShape = apply(reference, Annotations())

  def apply(reference: String, ast: YPart): UnresolvedShape = apply(reference, Annotations(ast))

  def apply(reference: String, annotations: Annotations): UnresolvedShape =
    UnresolvedShape(Fields(), annotations, reference)
}
