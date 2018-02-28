package amf.plugins.document.vocabularies2.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Iri
import amf.core.metamodel.document.DocumentModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies2.model.document.DialectInstance

object DialectInstanceModel extends DocumentModel {

  val DefinedBy = Field(Iri, Namespace.Meta + "definedBy")

  override def modelInstance: AmfObject = DialectInstance()

  override val `type`: List[ValueType] =
    Namespace.Meta + "DialectInstance" :: DocumentModel.`type`

  override def fields: List[Field] = DefinedBy :: DocumentModel.fields
}
