package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Fields, UnresolvedReference}
import org.yaml.model.{YNode, YPart}

/**
  * Unresolved shape: intended to be resolved after parsing (exception is thrown if shape is not resolved).
  */
case class UnresolvedShape(override val fields: Fields,
                           override val annotations: Annotations,
                           override val reference: String)
    extends AnyShape(fields, annotations)
    with UnresolvedReference {

  override def linkCopy(): AnyShape = this

  /** Resolve [[UnresolvedShape]] as link to specified target. */
  def resolve(target: Shape): Shape = target.link(reference, annotations).asInstanceOf[Shape].withName(name.value())

  override def meta: Obj = ShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/unresolved"
}

object UnresolvedShape {
  def apply(reference: String): UnresolvedShape = apply(reference, Annotations())

  def apply(reference: String, ast: YPart): UnresolvedShape = apply(reference, Annotations(ast))

  def apply(reference: String, ast: Option[YPart]): UnresolvedShape =
    apply(reference, Annotations(ast.getOrElse(YNode.Null)))

  def apply(reference: String, annotations: Annotations): UnresolvedShape =
    UnresolvedShape(Fields(), annotations, reference)
}
