package amf.shapes.client.scala.model.domain.grpc

import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.domain.metamodel.AnyShapeModel._

protected[amf] trait GrpcShapeFields { self: Shape =>

  def reservedValues: Seq[Reserved] = fields.field(ReservedValues)

  def withReserved(reserved: Reserved, ann: Annotations = Annotations()): this.type =
    setWithoutId(ReservedValues, AmfArray(reservedValues :+ reserved, ann), ann)
  def withReservedValues(newReservedValues: Seq[Reserved], ann: Annotations = Annotations()): this.type =
    setWithoutId(ReservedValues, AmfArray(newReservedValues ++ reservedValues, ann), ann)
}
