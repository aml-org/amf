package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.VocabularyReference


object VocabularyReferenceModel extends DomainElementModel {
  val Alias = Field(Str, Namespace.Schema + "alias")
  val Reference = Field(Str, Namespace.Document + "Reference")
  val Base = Field(Str, Namespace.Meta + "base" )

  override def modelInstance: AmfObject = VocabularyReference()

  override def fields: List[Field] = Alias :: Reference :: Base :: DomainElementModel.fields

  override val `type`: List[ValueType] = Namespace.Meta + "VocabularyReference" :: DomainElementModel.`type`
}
