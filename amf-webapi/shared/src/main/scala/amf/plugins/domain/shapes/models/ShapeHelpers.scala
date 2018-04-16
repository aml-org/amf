package amf.plugins.domain.shapes.models

import amf.core.annotations.ExplicitField
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{Linkable, RecursiveShape, Shape}
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression

trait ShapeHelpers { this: Shape =>

  def fromTypeExpression: Boolean = this.annotations.contains(classOf[ParsedFromTypeExpression])

  def fromExternalSource: Boolean = this match {
    case any: AnyShape => any.referenceId.option().isDefined
    case _             => false
  }

  def typeExpression: String = this.annotations.find(classOf[ParsedFromTypeExpression]) match {
    case Some(expr: ParsedFromTypeExpression) => expr.value
    case _                                    => throw new Exception("Trying to extract non existent type expression")
  }

  def externalSourceID: Option[String] = this match {
    case any: AnyShape => any.referenceId.option()
    case _             => None
  }

  def cloneShape(withRecursionBase: Option[String] = None, traversed: Set[String] = Set()): this.type = {
    if (traversed.contains(this.id)) {
      buildFixPoint(withRecursionBase, this).asInstanceOf[this.type]
    } else {
      val cloned: Shape = this match {
        case _: Linkable if this.isLink            => buildFixPoint(withRecursionBase, this)
        case _: RecursiveShape                     => RecursiveShape(annotations)
        case _: UnionShape                         => UnionShape(annotations)
        case _: ScalarShape                        => ScalarShape(annotations)
        case _: ArrayShape                         => ArrayShape(annotations)
        case _: MatrixShape                        => MatrixShape(annotations)
        case _: TupleShape                         => TupleShape(annotations)
        case _: PropertyShape                      => PropertyShape(annotations)
        case _: FileShape                          => FileShape(annotations)
        case _: NilShape                           => NilShape(annotations)
        case _: NodeShape                          => NodeShape(annotations)
        case _: SchemaShape                        => SchemaShape(annotations)
        case UnresolvedShape(_, annots, reference) => UnresolvedShape(reference, annots)
        case _: AnyShape                           => AnyShape(annotations)
      }
      cloned.id = this.id
      copyFields(cloned, withRecursionBase, traversed + this.id)
      if (cloned.isInstanceOf[NodeShape]) {
        cloned.add(ExplicitField())
      }
      cloned.asInstanceOf[this.type]
    }
  }

  protected def buildFixPoint(id: Option[String], link: Shape): RecursiveShape = {
    val fixPointId = id.getOrElse(link.id)
    RecursiveShape().withId(link.id).withFixPoint(fixPointId)
  }

}
