package amf.shapes.client.scala.model.domain.grpc

import amf.core.client.scala.model.IntField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.grpc.RangeModel
import amf.shapes.internal.domain.metamodel.grpc.RangeModel._

case class Range private[amf] (fields: Fields, annotations: Annotations) extends DomainElement {

  def from: IntField = fields.field(From)
  def to: IntField   = fields.field(To)

  def withFrom(from: Int): this.type = set(From, from)
  def withTo(from: Int): this.type   = set(To, from)

  override def meta: RangeModel.type = RangeModel

  /** Value, path + field value used to compose the id when the object is adopted */
  override def componentId: String = "/range"
}

object Range {
  def apply(): Range                         = apply(Annotations())
  def apply(annotations: Annotations): Range = new Range(Fields(), annotations)
  def apply(from: Int, to: Int, annotations: Annotations): Range =
    new Range(Fields(), annotations).withFrom(from).withTo(to)
}
