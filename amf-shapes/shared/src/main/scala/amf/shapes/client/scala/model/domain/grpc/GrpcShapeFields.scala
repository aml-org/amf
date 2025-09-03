package amf.shapes.client.scala.model.domain.grpc

import amf.core.client.scala.model.domain.Shape
import amf.shapes.internal.domain.metamodel.AnyShapeModel._

protected[amf] trait GrpcShapeFields { self: Shape =>

  def reservedValues: Seq[Reserved] = fields.field(ReservedValues)

  def withReserved(reserved: Reserved): this.type = setArrayWithoutId(ReservedValues, reservedValues :+ reserved)
  def withReservedValues(newReservedValues: Seq[Reserved]): this.type =
    setArrayWithoutId(ReservedValues, newReservedValues ++ reservedValues)
}
