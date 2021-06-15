package amf.plugins.domain.shapes.models

import amf.core.client.scala.model.domain.{AmfArray, DomainElement, Linkable, Shape}
import amf.core.client.scala.model.{BoolField, IntField}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
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

  override def adopted(parent: String, cycle: Seq[String] = Seq()): this.type = {
    val isCycle = cycle.contains(id)
    if ((parent + "").contains("#")) // TODO: NULL ARRIVING HERE
      simpleAdoption(parent)
    else
      simpleAdoption(parent + "#/")
    if (!isCycle)
      fields.entry(Items) match {
        case Some(items) =>
          items.value.value match {
            case shape: Shape =>
              shape.adopted(id + "/items", cycle :+ id)
            case arr: AmfArray =>
              simpleAdoption(id + s"/itemsTuple${arr.values.length}")
          }
        case _ => // ignore
      }
    this
  }

  override def ramlSyntaxKey: String = "arrayShape"
}

case class ArrayShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Shape                  = fields.field(Items)
  def contains: Shape               = fields.field(Contains)
  def minContains: Int              = fields.field(MinContains)
  def maxContains: Int              = fields.field(MaxContains)
  def unevaluatedItems: Boolean     = fields.field(UnevaluatedItems)
  def unevaluatedItemsSchema: Shape = fields.field(UnevaluatedItemsSchema)

  def withItems(items: Shape): this.type = {
    fields.set(id + "/items", Items, items, Annotations())
    this
  }

  def withUnevaluatedItems(value: Boolean): this.type      = set(UnevaluatedItems, value)
  def withUnevaluatedItemsSchema(schema: Shape): this.type = set(UnevaluatedItemsSchema, schema)
  def withContains(contains: Shape): this.type             = set(Contains, contains)
  def withMinContains(minTimes: Int): this.type            = set(MinContains, minTimes)
  def withMaxContains(maxTimes: Int): this.type            = set(MaxContains, maxTimes)

  def hasItems = Option(fields.field(Items)).isDefined

  def toMatrixShape: MatrixShape = MatrixShape(fields, annotations).withId(id)

  def toMatrixShapeWithoutId: MatrixShape = MatrixShape(fields, annotations)

  override def linkCopy(): ArrayShape = ArrayShape().withId(id)

  override val meta: AnyShapeModel = ArrayShapeModel

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ArrayShape.apply
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

  def toArrayShape = {
    val array = ArrayShape(fields, annotations)
    Option(id) match {
      case Some(effectiveId: String) =>
        val res = array.withId(effectiveId)
        res
      case None =>
        array
    }
  }
  def toMatrixShape: MatrixShape = this

  override def linkCopy() = MatrixShape().withId(id)

  override val meta: AnyShapeModel = MatrixShapeModel

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = MatrixShape.apply
}

object MatrixShape {

  def apply(): MatrixShape = apply(Annotations())

  def apply(ast: YPart): MatrixShape = apply(Annotations(ast))

  def apply(annotations: Annotations): MatrixShape = MatrixShape(Fields(), annotations)

}

case class TupleShape(override val fields: Fields, override val annotations: Annotations)
    extends DataArrangementShape(fields, annotations) {

  def items: Seq[Shape]                                = fields.field(TupleItems)
  def withItems(items: Seq[Shape]): this.type          = setArray(TupleItems, items)
  def closedItems: BoolField                           = fields.field(TupleShapeModel.ClosedItems)
  def additionalItemsSchema: Shape                     = fields.field(TupleShapeModel.AdditionalItemsSchema)
  def withClosedItems(closedItems: Boolean): this.type = set(TupleShapeModel.ClosedItems, closedItems)

  override def linkCopy() = TupleShape().withId(id)

  override val meta: AnyShapeModel = TupleShapeModel

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = TupleShape.apply
}

object TupleShape {

  def apply(): TupleShape = apply(Annotations())

  def apply(ast: YPart): TupleShape = apply(Annotations(ast))

  def apply(annotations: Annotations): TupleShape = TupleShape(Fields(), annotations)

}
