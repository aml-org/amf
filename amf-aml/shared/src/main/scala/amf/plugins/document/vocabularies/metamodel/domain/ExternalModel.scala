package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.External

object ExternalModel extends DomainElementModel {

  val Alias = Field(Str, Namespace.Schema + "alias")
  val Base = Field(Str, Namespace.Meta + "base")

  override def modelInstance: AmfObject = External()

  override def fields: List[Field] = Alias :: Base :: DomainElementModel.fields

  override val `type`: List[ValueType] = Namespace.Owl + "Ontology" :: Namespace.Meta + "ExternalVocabulary" :: DomainElementModel.`type`
}
