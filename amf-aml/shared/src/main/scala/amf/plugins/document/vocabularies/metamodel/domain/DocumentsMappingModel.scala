package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Iri, Str}
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.{DocumentMapping, DocumentsModel, PublicNodeMapping}

object PublicNodeMappingModel extends DomainElementModel {

  val Name =
    Field(Str, Namespace.Schema + "name", ModelDoc(ExternalModelVocabularies.SchemaOrg, "name", "Name of the mapping"))
  val MappedNode = Field(
    Iri,
    Namespace.Meta + "mappedNode",
    ModelDoc(ModelVocabularies.Meta, "mapped node", "Node in the dialect definition associated to this mapping"))

  override def fields: List[Field] = Name :: MappedNode :: DomainElementModel.fields

  override def modelInstance: AmfObject = PublicNodeMapping()

  override val `type`: List[ValueType] = Namespace.Meta + "PublicNodeMapping" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Public Node Mapping",
    "Mapping for a graph node mapping to a particular function in a dialect"
  )
}

object DocumentMappingModel extends DomainElementModel {

  val DocumentName = Field(Str,
                           Namespace.Meta + "name",
                           ModelDoc(ModelVocabularies.Meta, "name", "Name of the document for a dialect base unit"))
  val EncodedNode = Field(
    Iri,
    Namespace.Meta + "encodedNode",
    ModelDoc(ModelVocabularies.Meta, "encoded node", "Node in the dialect encoded in the target mapped base unit"))
  val DeclaredNodes = Field(
    Array(PublicNodeMappingModel),
    Namespace.Meta + "declaredNode",
    ModelDoc(ModelVocabularies.Meta, "declared node", "Node in the dialect declared in the target mappend base unit")
  )

  override def fields: List[Field] = DocumentName :: EncodedNode :: DeclaredNodes :: DomainElementModel.fields

  override def modelInstance: AmfObject = DocumentMapping()

  override val `type`: List[ValueType] = Namespace.Meta + "DocumentMapping" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Document Mapping",
    "Mapping for a particular dialect document into a graph base unit"
  )
}

object DocumentsModelModel extends DomainElementModel {

  val Root = Field(
    DocumentMappingModel,
    Namespace.Meta + "rootDocument",
    ModelDoc(ModelVocabularies.Meta, "root document", "Root node encoded in a mapped document base unit")
  )
  val Fragments = Field(
    Array(DocumentMappingModel),
    Namespace.Meta + "fragments",
    ModelDoc(ModelVocabularies.Meta, "fragments", "Mapping of fragment base unit for a particular dialect")
  )
  val Library = Field(
    DocumentMappingModel,
    Namespace.Meta + "library",
    ModelDoc(ModelVocabularies.Meta, "library", "Mappig of module base unit for a particular dialect"))
  // options:
  val SelfEncoded = Field(
    Bool,
    Namespace.Meta + "selfEncoded",
    ModelDoc(
      ModelVocabularies.Meta,
      "self encoded",
      "Information about if the base unit URL should be the same as the URI of the parsed root nodes in the unit")
  )
  val DeclarationsPath = Field(
    Str,
    Namespace.Meta + "declarationsPath",
    ModelDoc(ModelVocabularies.Meta,
             "declarations path",
             "Information about the AST location of the declarations to be parsed as declared domain elements")
  )

  override def fields: List[Field] =
    Root :: Fragments :: Library :: SelfEncoded :: DeclarationsPath :: DomainElementModel.fields

  override def modelInstance: AmfObject = DocumentsModel()

  override val `type`: List[ValueType] = Namespace.Meta + "DocumentsModel" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "Documents Model",
    "Mapping from different type of dialect documents to base units in the parsed graph"
  )
}
