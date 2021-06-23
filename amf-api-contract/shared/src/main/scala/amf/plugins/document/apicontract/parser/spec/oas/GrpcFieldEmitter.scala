package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.shapes.client.scala.model.domain.ArrayShape

class GrpcFieldEmitter(property: PropertyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) extends GrpcEmitter {

  def emit(): Unit = {
    builder += (s"$repeated${fieldRange(property.range)} $fieldName = $fieldNumber;", position)
  }

  def position: Position = pos(property.range.annotations)

  def fieldName: String = property.displayName.option().getOrElse(property.name.value())

  def fieldNumber: Int = property.serializationOrder.option().getOrElse(0)


  def repeated: String = if (property.range.isInstanceOf[ArrayShape]) { "repeated " } else { "" }

}

object GrpcFieldEmitter {
  def apply(property: PropertyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) = new GrpcFieldEmitter(property, builder, ctx)
}