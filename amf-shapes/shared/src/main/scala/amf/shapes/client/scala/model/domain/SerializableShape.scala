package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.domain.ShapeModel.SerializationSchema

trait SerializableShape { this: AnyShape =>
  def serializationSchema: Shape                        = fields.field(SerializationSchema)
  def withSerializationSchema(schema: Shape): this.type = set(SerializationSchema, schema)
}
