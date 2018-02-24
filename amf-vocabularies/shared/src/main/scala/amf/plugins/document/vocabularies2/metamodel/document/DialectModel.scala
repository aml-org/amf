package amf.plugins.document.vocabularies2.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.document.DocumentModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies2.model.document.Dialect

object DialectModel extends DocumentModel {

  val Name = Field(Str, Namespace.Schema + "name")
  val Version = Field(Str, Namespace.Schema + "version")

  override def modelInstance: AmfObject = Dialect()

  override val `type`: List[ValueType] =
    Namespace.Meta + "Dialect" :: DocumentModel.`type`

  override def fields: List[Field] = Name :: Version ::  DocumentModel.fields
}
