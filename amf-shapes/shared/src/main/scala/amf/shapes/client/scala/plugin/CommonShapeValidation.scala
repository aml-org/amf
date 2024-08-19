package amf.shapes.client.scala.plugin

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.{AnyShape, SchemaShape}
import amf.shapes.internal.annotations.AVROSchemaType

trait CommonShapeValidation {

  protected def isAnyShape(shape: Shape): Boolean = shape match {
    case _: SchemaShape => false
    case _: AnyShape    => true
    case _              => false
  }

  protected def isAvroSchemaShape(shape: Shape): Boolean = shape.annotations.contains(classOf[AVROSchemaType])
}
