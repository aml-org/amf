package amf.apicontract.client.scala.model.document

import amf.apicontract.internal.metamodel.document.JsonSchemaDocumentModel
import amf.apicontract.internal.metamodel.document.JsonSchemaDocumentModel.SchemaVersion
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.{Annotations, Fields}

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
