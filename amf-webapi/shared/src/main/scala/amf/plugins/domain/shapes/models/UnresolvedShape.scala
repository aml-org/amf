package amf.plugins.domain.shapes.models

import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Fields, UnresolvedReference}
import amf.plugins.document.webapi.parser.spec.common.ShapeExtensionParser
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import org.yaml.model.{YNode, YPart}

/**
  * Unresolved shape: intended to be resolved after parsing (exception is thrown if shape is not resolved).
  */
case class UnresolvedShape(override val fields: Fields,
                           override val annotations: Annotations,
                           override val reference: String,
                           fatherExtensionParser: Option[ShapeExtensionParser] = None)
    extends AnyShape(fields, annotations)
    with UnresolvedReference {

  override def linkCopy(): AnyShape = this

  /*
  override def withId(newId: String): this.type = {
    if (id == null) super.withId(newId)
    this
  }
   */

  /** Resolve [[UnresolvedShape]] as link to specified target. */
  def resolve(target: Shape): Shape = target.link(reference, annotations).asInstanceOf[Shape].withName(name.value())

  override def meta: AnyShapeModel = AnyShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/unresolved"

  override def afterResolve(): Unit = fatherExtensionParser.foreach { parser =>
    parser.parse()
  }

  // if is unresolved the effective target its himselft, because any real type has been found.
  override def effectiveLinkTarget = this

  override def copyShape(): UnresolvedShape =
    UnresolvedShape(fields.copy(), annotations.copy(), reference, fatherExtensionParser).withId(id)

}

object UnresolvedShape {
  def apply(reference: String): UnresolvedShape = apply(reference, Annotations(), None)

  def apply(reference: String, ast: YPart, extensionParser: Option[ShapeExtensionParser]): UnresolvedShape =
    apply(reference, Annotations(ast), extensionParser)

  def apply(reference: String, ast: YPart): UnresolvedShape = apply(reference, Annotations(ast), None)

  def apply(reference: String, ast: Option[YPart]): UnresolvedShape =
    apply(reference, Annotations(ast.getOrElse(YNode.Null)), None)

  def apply(reference: String,
            annotations: Annotations,
            extensionParser: Option[ShapeExtensionParser]): UnresolvedShape =
    UnresolvedShape(Fields(), annotations, reference, extensionParser)
}
