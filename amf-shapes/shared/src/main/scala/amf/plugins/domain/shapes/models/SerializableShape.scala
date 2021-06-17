package amf.plugins.domain.shapes.models

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.domain.ShapeModel.SerializationSchema
import amf.shapes.client.scala.model.domain.AnyShape

trait SerializableShape { this: AnyShape =>
  def serializationSchema: Shape = fields.field(SerializationSchema)
  def withSerializationSchema(schema: Shape): this.type = set(SerializationSchema, schema)
}
