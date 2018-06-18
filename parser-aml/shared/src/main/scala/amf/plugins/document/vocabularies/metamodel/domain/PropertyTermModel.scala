package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.{DatatypePropertyTerm, ObjectPropertyTerm}

abstract class PropertyTermModel extends DomainElementModel {
  val Name = Field(Str, Namespace.Schema + "name")
  val DisplayName = Field(Str, Namespace.Meta + "displayName")
  val Description = Field(Str, Namespace.Schema + "description")
  val Range       = Field(Iri, Namespace.Rdfs + "range")
  val SubPropertyOf  = Field(Array(Iri), Namespace.Rdfs + "subPropertyOf")

  override def fields: List[Field] = DisplayName :: Description :: Range :: SubPropertyOf :: DomainElementModel.fields
}


object ObjectPropertyTermModel extends PropertyTermModel {
  override val `type`: List[ValueType] = Namespace.Owl + "ObjectProperty" :: Namespace.Meta + "Property" :: DomainElementModel.`type`
  override def modelInstance: AmfObject = ObjectPropertyTerm()
}

object DatatypePropertyTermModel extends PropertyTermModel {
  override val `type`: List[ValueType] = Namespace.Owl + "DatatypeProperty" :: Namespace.Meta + "Property" :: DomainElementModel.`type`
  override def modelInstance: AmfObject = DatatypePropertyTerm()
}