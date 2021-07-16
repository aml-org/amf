package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.client.scala.model.{BoolField, IntField}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils._
import amf.shapes.internal.domain.metamodel.ArrayShapeModel._

/**
  * Array shape
  */
abstract class DataArrangementShape private[amf] (fields: Fields, annotations: Annotations)
    extends AnyShape(fields, annotations) {
  def minItems: IntField = fields.field(MinItems)

  def maxItems: IntField = fields.field(MaxItems)

  def uniqueItems: BoolField = fields.field(UniqueItems)

  def collectionFormat: String = fields.field(CollectionFormat)

  def withMinItems(minItems: Int): this.type = set(MinItems, minItems)

  def withMaxItems(maxItems: Int): this.type = set(MaxItems, maxItems)

  def withUniqueItems(uniqueItems: Boolean): this.type = set(UniqueItems, uniqueItems)

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

  private[amf] override def componentId: String =
    "/array/" + name.option().getOrElse("default-array").urlComponentEncoded

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

  private[amf] override def ramlSyntaxKey: String = "arrayShape"
}
