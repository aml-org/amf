package amf.shapes.client.scala.model.document

import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.document.metamodel.AvroSchemaDocumentModel

class AvroSchemaDocument(override val fields: Fields, override val annotations: Annotations)
    extends Document(fields, annotations) {
  override def encodes: Shape                     = super.encodes.asInstanceOf[Shape]
  override def meta: AvroSchemaDocumentModel.type = AvroSchemaDocumentModel
}

object AvroSchemaDocument {
  def apply(): AvroSchemaDocument                         = apply(Annotations())
  def apply(annotations: Annotations): AvroSchemaDocument = new AvroSchemaDocument(Fields(), annotations)
}
