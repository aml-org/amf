package amf.shapes.client.scala.model.domain.grpc

import amf.core.client.scala.model.IntField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.grpc.ReservedRangeModel
import amf.shapes.internal.domain.metamodel.grpc.ReservedRangeModel._

case class ReservedRange private[amf](fields: Fields, annotations: Annotations) extends DomainElement {

  def from: IntField = fields.field(From)
  def to: IntField   = fields.field(To)

  def withFrom(from: Int): this.type = set(From, from)
  def withTo(from: Int): this.type   = set(To, from)

  override def meta: ReservedRangeModel.type = ReservedRangeModel

  /** Value, path + field value used to compose the id when the object is adopted */
  override def componentId: String = "/reserved-range"
}

object ReservedRange {
  def apply(): ReservedRange                         = apply(Annotations())
  def apply(annotations: Annotations): ReservedRange = new ReservedRange(Fields(), annotations)
  def apply(from: Int, to: Int, annotations: Annotations): ReservedRange =
    new ReservedRange(Fields(), annotations).withFrom(from).withTo(to)
}
