package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.shapes.client.scala.model.domain.{
  AnyShape,
  ArrayShape,
  FileShape,
  MatrixShape,
  NilShape,
  NodeShape,
  ScalarShape,
  TupleShape,
  UnionShape
}

trait Frame {
  val shape: Shape
  protected def prettyPrintShape: String = {
    val name = shape.name.value()
    shape match {
      case _: UnionShape     => s"UnionShape($name)"
      case _: ScalarShape    => s"ScalarShape($name)"
      case _: ArrayShape     => s"ArrayShape($name)"
      case _: MatrixShape    => s"MatrixShape($name)"
      case _: TupleShape     => s"TupleShape($name)"
      case _: PropertyShape  => s"PropertyShape($name)"
      case _: FileShape      => s"FileShape($name)"
      case _: NilShape       => s"NilShape($name)"
      case _: NodeShape      => s"NodeShape($name)"
      case _: AnyShape       => s"AnyShape($name)"
      case _: RecursiveShape => s"RecursiveShape($name)"
    }
  }
}
