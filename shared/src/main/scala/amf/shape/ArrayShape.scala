package amf.shape

import amf.common.AMFAST
import amf.domain.{Annotations, Fields}
import amf.metadata.shape.ArrayShapeModel._
import org.yaml.model.YMapEntry

/**
  * Array shape
  */

abstract class DataArrangementShape() extends Shape {
  def minItems: Int        = fields(MinItems)
  def maxItems: Int        = fields(MaxItems)
  def uniqueItems: Boolean = fields(UniqueItems)

  def withMinItems(minItems: Int)           = set(MinItems, minItems)
  def withMaxItems(maxItems: Int)           = set(MaxItems, maxItems)
  def withUniqueItems(uniqueItems: Boolean) = set(UniqueItems, uniqueItems)

  def withScalarItems(): ScalarShape = {
    val scalar = ScalarShape()
    this.set(Items, scalar)
    scalar
  }

  def withNodeItems(): NodeShape = {
    val node = NodeShape()
    this.set(Items, node)
    node
  }

  def withArrayItems(): ArrayShape = {
    val array = ArrayShape()
    this.set(Items, array)
    array
  }

  override def adopted(parent: String): this.type = withId(parent + "/array/" + name)
}

case class ArrayShape(fields: Fields, annotations: Annotations) extends DataArrangementShape {
  def items: Shape         = fields(Items)
  def withItems(items: Shape)               = set(Items, items)

  def toMatrixShape: MatrixShape = MatrixShape(fields, annotations)
}

object ArrayShape {

  def apply(): ArrayShape = apply(Annotations())

  def apply(ast: YMapEntry): ArrayShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ArrayShape = ArrayShape(Fields(), annotations)

}

case class MatrixShape(fields: Fields, annotations: Annotations) extends DataArrangementShape {
  def items: Shape         = fields(Items)
  def withItems(items: Shape)               = set(Items, items)

  def toArrayShape = ArrayShape(fields, annotations)
  def toMatrixShape = this
}

object MatrixShape {

  def apply(): MatrixShape = apply(Annotations())

  def apply(ast: AMFAST): MatrixShape = apply(Annotations(ast))

  def apply(annotations: Annotations): MatrixShape = MatrixShape(Fields(), annotations)

}


case class TupleShape(fields: Fields, annotations: Annotations) extends DataArrangementShape {
  def items: Seq[Shape]             = fields(Items)
  def withItems(items: Seq[Shape])  = setArray(Items, items)
}

object TupleShape {

  def apply(): TupleShape = apply(Annotations())

  def apply(ast: YMapEntry): TupleShape = apply(Annotations(ast))

  def apply(annotations: Annotations): TupleShape = TupleShape(Fields(), annotations)

}
