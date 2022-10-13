package amf.shapes.internal.document.metamodel

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Document
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.document.{BaseUnitModel, DocumentModel}
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDElementModel

object JsonLDInstanceDocumentModel extends BaseUnitModel {

  val Encodes: Field = Field(
    Array(JsonLDElementModel),
    Document + "encodes",
    ModelDoc(
      ModelVocabularies.AmlDoc,
      "encodes",
      "The encodes relationship links a parsing Unit with the DomainElement from a particular domain the unit contains."
    )
  )

  override val `type`: List[ValueType]  = (Document + "JsonLDInstanceDocument") :: DocumentModel.`type`
  override def modelInstance: AmfObject = JsonLDInstanceDocument()
  override def fields: List[Field]      = Encodes +: BaseUnitModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "JsonLDInstanceDocument",
    "A Document that represents a JSON-LD instance document"
  )
}
