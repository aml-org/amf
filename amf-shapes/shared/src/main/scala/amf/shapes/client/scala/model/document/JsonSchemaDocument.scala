package amf.shapes.client.scala.model.document

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.document.metamodel.JsonSchemaDocumentModel
import amf.shapes.internal.document.metamodel.JsonSchemaDocumentModel.SchemaVersion

class JsonSchemaDocument(override val fields: Fields, override val annotations: Annotations)
    extends Document(fields, annotations) {

  override def encodes: Shape = super.encodes.asInstanceOf[Shape]

  def schemaVersion: StrField = fields.field(SchemaVersion)

  override def meta = JsonSchemaDocumentModel

}

object JsonSchemaDocument {
  def apply(): JsonSchemaDocument = apply(Annotations())

  def apply(annotations: Annotations): JsonSchemaDocument = new JsonSchemaDocument(Fields(), annotations)
}
