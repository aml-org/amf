package amf.shapes.internal.document.metamodel

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.document.{AvroSchemaDocument, JsonSchemaDocument}

/** AVRO Schema Fragment metamodel
  *
  * An AVRO Schema Fragment is a parsing Unit that encodes a single Shape. It main purpose is to expose the encoded
  * shape so it can be re-used
  */
object AvroSchemaDocumentModel extends DocumentModel {
  override val `type`: List[ValueType] = Namespace.Document + "AvroSchemaDocumentModel" :: DocumentModel.`type`

  override val fields: List[Field] = DocumentModel.fields

  override def modelInstance: AmfObject = AvroSchemaDocument()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "AvroSchemaDocument",
    "A Document that represents an AVRO Schema Fragment"
  )
}
