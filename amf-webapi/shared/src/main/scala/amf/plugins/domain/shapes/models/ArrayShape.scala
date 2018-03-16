package amf.plugins.domain.shapes.models

import amf.client.model.{BoolField, IntField}
import amf.core.metamodel.Obj
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.ArrayShapeModel._
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, MatrixShapeModel, TupleShapeModel}
import org.yaml.model.YPart

/**
  * Array shape
  */
abstract class DataArrangementShape(fields: Fields, annotations: Annotations) extends AnyShape(fields, annotations) {

  def minItems: IntField     = fields.field(MinItems)
  def maxItems: IntField     = fields.field(MaxItems)
  def uniqueItems: BoolField = fields.field(UniqueItems)

  def withMinItems(minItems: Int): this.type           = set(MinItems, minItems)
  def withMaxItems(maxItems: Int): this.type           = set(MaxItems, maxItems)
  def withUniqueItems(uniqueItems: Boolean): this.type = set(UniqueItems, uniqueItems)

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

  override def adopted(parent: String): this.type = {
    withId(parent + "/array/" + name.value())
    fields.entry(Items) match {
      case Some(items) =>
        items.value.value match {
          case shape: Shape => shape.adopted(id)
        }
      case _ => // ignore
    }
    this
  }
}

case class ArrayShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Shape = fields.field(Items)

  def withItems(items: Shape): this.type = set(Items, items)

  def toMatrixShape: MatrixShape = MatrixShape(fields, annotations)

  override def linkCopy(): ArrayShape = ArrayShape().withId(id)

  override def meta: Obj = ArrayShapeModel
}

object ArrayShape {

  def apply(): ArrayShape = apply(Annotations())

  def apply(ast: YPart): ArrayShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ArrayShape = ArrayShape(Fields(), annotations)

}

case class MatrixShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Shape = fields.field(Items)

  def withItems(items: Shape): this.type = set(Items, items)

  def toArrayShape               = ArrayShape(fields, annotations)
  def toMatrixShape: MatrixShape = this

  override def linkCopy() = MatrixShape().withId(id)

  override def meta: Obj = MatrixShapeModel
}

object MatrixShape {

  def apply(): MatrixShape = apply(Annotations())

  def apply(ast: YPart): MatrixShape = apply(Annotations(ast))

  def apply(annotations: Annotations): MatrixShape = MatrixShape(Fields(), annotations)

}

case class TupleShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Seq[Shape] = fields.field(Items)

  def withItems(items: Seq[Shape]): this.type = setArray(Items, items)

  override def linkCopy() = TupleShape().withId(id)

  override def adopted(parent: String): this.type = {
    withId(parent + "/array/" + name.value())
    items.foreach(_.adopted(id))
    this
  }

  override def meta: Obj = TupleShapeModel
}

object TupleShape {

  def apply(): TupleShape = apply(Annotations())

  def apply(ast: YPart): TupleShape = apply(Annotations(ast))

  def apply(annotations: Annotations): TupleShape = TupleShape(Fields(), annotations)

}
