package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, Str, Bool}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.{DocumentMapping, DocumentsModel, PublicNodeMapping}

object PublicNodeMappingModel extends DomainElementModel {

  val Name = Field(Str, Namespace.Schema + "name")
  val MappedNode = Field(Iri, Namespace.Meta + "mappedNode")


  override def fields: List[Field] = Name :: MappedNode :: DomainElementModel.fields

  override def modelInstance: AmfObject = PublicNodeMapping()

  override val `type`: List[ValueType] = Namespace.Meta + "PublicNodeMapping" :: DomainElementModel.`type`
}


object DocumentMappingModel extends DomainElementModel {

  val DocumentName = Field(Str, Namespace.Meta + "name")
  val EncodedNode = Field(Iri, Namespace.Meta + "encodedNode")
  val DeclaredNodes = Field(Array(PublicNodeMappingModel), Namespace.Meta + "declaredNode")

  override def fields: List[Field] = DocumentName :: EncodedNode :: DeclaredNodes :: DomainElementModel.fields

  override def modelInstance: AmfObject = DocumentMapping()

  override val `type`: List[ValueType] = Namespace.Meta + "DocumentMapping" :: DomainElementModel.`type`
}


object DocumentsModelModel extends DomainElementModel {

  val Root = Field(DocumentMappingModel, Namespace.Meta + "rootDocument")
  val Fragments = Field(Array(DocumentMappingModel), Namespace.Meta + "fragments")
  val Library = Field(DocumentMappingModel, Namespace.Meta + "library")
  val SelfEncoded = Field(Bool, Namespace.Meta + "selfEncoded")

  override def fields: List[Field] = Root :: Fragments :: Library :: SelfEncoded :: DomainElementModel.fields

  override def modelInstance: AmfObject = DocumentsModel()

  override val `type`: List[ValueType] = Namespace.Meta + "DocumentsModel" :: DomainElementModel.`type`
}
