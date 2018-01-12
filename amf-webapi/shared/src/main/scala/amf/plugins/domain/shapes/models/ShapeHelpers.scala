package amf.plugins.domain.shapes.models

import amf.core.annotations.ExplicitField
import amf.core.model.domain.{Linkable, RecursiveShape, Shape}
import amf.core.model.domain.extensions.PropertyShape
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression

trait ShapeHelpers { this: Shape =>

  def fromTypeExpression: Boolean = this.annotations.contains(classOf[ParsedFromTypeExpression])
  def typeExpression: String = this.annotations.find(classOf[ParsedFromTypeExpression]) match {
    case Some(expr: ParsedFromTypeExpression) => expr.value
    case _                                    => throw new Exception("Trying to extract non existent type expression")
  }

  def cloneShape(withRecursionBase: Option[String] = None): this.type = {
    val cloned: Shape = this match {
      case _: Linkable if this.isLink => buildFixPoint(withRecursionBase, this)
      case _: UnionShape    => UnionShape()
      case _: ScalarShape   => ScalarShape()
      case _: ArrayShape    => ArrayShape()
      case _: MatrixShape   => MatrixShape()
      case _: TupleShape    => TupleShape()
      case _: PropertyShape => PropertyShape()
      case _: FileShape     => FileShape()
      case _: NilShape      => NilShape()
      case _: NodeShape     => NodeShape()
      case _: SchemaShape   => SchemaShape()
      case UnresolvedShape(_, annots, reference) => UnresolvedShape(reference, annots)
      case _: AnyShape      => AnyShape()
    }
    cloned.id = this.id
    copyFields(cloned, withRecursionBase)
    if (cloned.isInstanceOf[NodeShape]) {
      cloned.add(ExplicitField())
    }
    cloned.asInstanceOf[this.type]
  }

  protected def buildFixPoint(id: Option[String], link: Shape): RecursiveShape = {
    val fixPointId = id.getOrElse(link.id)
    RecursiveShape().withId(link.id).withFixPoint(fixPointId)
  }

}