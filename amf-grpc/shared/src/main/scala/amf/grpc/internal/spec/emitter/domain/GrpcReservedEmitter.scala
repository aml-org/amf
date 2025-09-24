package amf.grpc.internal.spec.emitter.domain

import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper.MAX_VALUE
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.client.scala.model.domain.grpc.Reserved

case class GrpcReservedEmitter(shape: AnyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) {
  def emit(): Unit = {
    shape.reservedValues.foreach(emitReserved)
  }

  private def emitReserved(reserved: Reserved): Unit = {
    emitReservedNumber(reserved)
    emitReservedFieldName(reserved)
    emitReservedRange(reserved)
  }

  private def emitReservedNumber(reserved: Reserved): Unit = {
    reserved.number.option().foreach { number =>
      builder += s"reserved $number;"
    }
  }

  private def emitReservedFieldName(reserved: Reserved): Unit = {
    reserved.fieldName.option().foreach { fieldName =>
      builder += s"""reserved "$fieldName";"""
    }
  }

  private def emitReservedRange(reserved: Reserved): Unit = {
    Option(reserved.range).foreach { range =>
      for {
        from <- range.from.option()
        to <- range.to.option()
      } yield {
        val toValue = if (to == MAX_VALUE) "max" else to.toString
        builder += s"reserved $from to $toValue;"
      }
    }
  }
}
