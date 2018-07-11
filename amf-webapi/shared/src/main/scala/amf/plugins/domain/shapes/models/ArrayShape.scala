package amf.plugins.domain.shapes.models

import amf.core.model.domain.Shape
import amf.core.model.{BoolField, IntField}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.Strings
import amf.plugins.domain.shapes.metamodel.ArrayShapeModel._
import amf.plugins.domain.shapes.metamodel.TupleShapeModel.TupleItems
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ArrayShapeModel, MatrixShapeModel, TupleShapeModel}
import org.yaml.model.YPart

/**
  * Array shape
  */
abstract class DataArrangementShape(fields: Fields, annotations: Annotations) extends AnyShape(fields, annotations) {
  def minItems: IntField       = fields.field(MinItems)
  def maxItems: IntField       = fields.field(MaxItems)
  def uniqueItems: BoolField   = fields.field(UniqueItems)
  def collectionFormat: String = fields.field(CollectionFormat)

  def withMinItems(minItems: Int): this.type                     = set(MinItems, minItems)
  def withMaxItems(maxItems: Int): this.type                     = set(MaxItems, maxItems)
  def withUniqueItems(uniqueItems: Boolean): this.type           = set(UniqueItems, uniqueItems)
  def withCollectionFormat(collectionFormat: Boolean): this.type = set(CollectionFormat, collectionFormat)

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

  override def componentId: String = "/array/" + name.option().getOrElse("default-array").urlComponentEncoded

  override def adopted(parent: String): this.type = {
    if ((parent + "").contains("#")) // TODO: NULL ARRIVING HERE
      simpleAdoption(parent)
    else
      simpleAdoption(parent + "#/")
    fields.entry(Items) match {
      case Some(items) =>
        items.value.value match {
          case shape: Shape => shape.adopted(id + "/items")
        }
      case _ => // ignore
    }
    this
  }

  override def ramlSyntaxKey: String = "arrayShape"
}

case class ArrayShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Shape = fields.field(Items)
  def withItems(items: Shape): this.type = {
    fields.set(id + "/items", Items, items, Annotations())
    this
  }

  def toMatrixShape: MatrixShape = MatrixShape(fields, annotations).withId(id)

  override def linkCopy(): ArrayShape = ArrayShape().withId(id)

  override def meta: AnyShapeModel = ArrayShapeModel

  override def copyShape(): ArrayShape = ArrayShape(fields.copy(), annotations.copy()).withId(id)

}

object ArrayShape {

  def apply(): ArrayShape = apply(Annotations())

  def apply(ast: YPart): ArrayShape = apply(Annotations(ast))

  def apply(annotations: Annotations): ArrayShape = ArrayShape(Fields(), annotations)

}

case class MatrixShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Shape                       = fields.field(Items)
  def withItems(items: Shape): this.type = set(Items, items)

  def toArrayShape               = ArrayShape(fields, annotations)
  def toMatrixShape: MatrixShape = this

  override def linkCopy() = MatrixShape().withId(id)

  override def meta: AnyShapeModel = MatrixShapeModel

  override def copyShape(): MatrixShape = MatrixShape(fields.copy(), annotations.copy()).withId(id)

}

object MatrixShape {

  def apply(): MatrixShape = apply(Annotations())

  def apply(ast: YPart): MatrixShape = apply(Annotations(ast))

  def apply(annotations: Annotations): MatrixShape = MatrixShape(Fields(), annotations)

}

case class TupleShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Seq[Shape]                        = fields.field(TupleItems)
  def withItems(items: Seq[Shape]): this.type  = setArray(TupleItems, items)
  def additionalItems: BoolField               = fields.field(TupleShapeModel.AdditionalItems)
  def withAdditionalItems(additional: Boolean) = set(TupleShapeModel.AdditionalItems, additional)

  override def linkCopy() = TupleShape().withId(id)

  override def meta: AnyShapeModel = TupleShapeModel

  override def copyShape(): TupleShape = TupleShape(fields.copy(), annotations.copy()).withId(id)
}

object TupleShape {

  def apply(): TupleShape = apply(Annotations())

  def apply(ast: YPart): TupleShape = apply(Annotations(ast))

  def apply(annotations: Annotations): TupleShape = TupleShape(Fields(), annotations)

}
