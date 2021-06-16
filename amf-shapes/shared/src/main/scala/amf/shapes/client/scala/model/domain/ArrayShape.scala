package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.ArrayShapeModel._
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, ArrayShapeModel}
import org.yaml.model.YPart

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
