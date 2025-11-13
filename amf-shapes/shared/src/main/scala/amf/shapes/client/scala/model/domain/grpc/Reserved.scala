package amf.shapes.client.scala.model.domain.grpc

import amf.core.client.scala.model.{IntField, StrField}
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.grpc.ReservedModel
import amf.shapes.internal.domain.metamodel.grpc.ReservedModel._

case class Reserved private[amf] (fields: Fields, annotations: Annotations) extends DomainElement {

  def range: ReservedRange = fields.field(ReservedModel.Range)
  def fieldName: StrField  = fields.field(FieldName)
  def number: IntField     = fields.field(Number)

  def withRange(range: ReservedRange, ann: Annotations = Annotations()): this.type =
    set(ReservedModel.Range, range, ann)
  def withFieldName(fieldName: String, ann: Annotations = Annotations()): this.type =
    set(FieldName, AmfScalar(fieldName, ann), ann)
  def withNumber(number: Int, ann: Annotations = Annotations()): this.type = set(Number, AmfScalar(number, ann), ann)

  override def meta: ReservedModel.type = ReservedModel

  /** Value, path + field value used to compose the id when the object is adopted */
  override def componentId: String = "/reserved"
}

object Reserved {
  def apply(): Reserved               = apply(Annotations())
  def apply(annotations: Annotations) = new Reserved(Fields(), annotations)
}
