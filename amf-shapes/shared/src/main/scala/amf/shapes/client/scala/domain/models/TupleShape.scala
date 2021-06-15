package amf.shapes.client.scala.domain.models

import amf.core.client.scala.model.BoolField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.TupleShapeModel.TupleItems
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, TupleShapeModel}
import org.yaml.model.YPart

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
