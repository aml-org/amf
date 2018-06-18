package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.ClassTerm

object ClassTermModel extends DomainElementModel {

  val Name = Field(Str, Namespace.Schema + "name")
  val DisplayName = Field(Str, Namespace.Meta + "displayName")
  val Description = Field(Str, Namespace.Schema + "description")
  val Properties  = Field(Array(Iri), Namespace.Meta + "properties")
  val SubClassOf  = Field(Array(Iri), Namespace.Rdfs + "subClassOf")

  override def modelInstance: AmfObject = ClassTerm()

  override def fields: List[Field] = DisplayName :: Description :: Properties :: SubClassOf :: DomainElementModel.fields

  override val `type`: List[ValueType] = Namespace.Owl + "Class" :: DomainElementModel.`type`
}
