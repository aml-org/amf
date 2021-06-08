package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, Iri, Str}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.Meta
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models._


trait WithIri {
  val IRI =
    Field(Iri, Meta + "iri", ModelDoc(ModelVocabularies.Meta, "iri", "Base IRI for all the elements in the model"))
}

object ContextElementWithIri extends WithIri

/**
  * Encodes information about the base document IRI for the model element @ids
  */
object BaseIRIModel extends DomainElementModel with WithIri {

  override val `type`: List[ValueType] = List(Meta + "ContextBaseIri")

  val Nulled =
    Field(Bool, Meta + "null", ModelDoc(ModelVocabularies.Meta, "null", "Marks the baseIRI as null, preventing generation of absolute IRIs in the model"))

  override def modelInstance: AmfObject = BaseIri()

  override def fields: List[Field] = List(IRI, Nulled)
}

/**
  * Encodes information about the base vocabulary to map by default properties and types in the model
  */
object DefaultVocabularyModel extends DomainElementModel with WithIri {

  override val `type`: List[ValueType] = List(Meta + "ContextDefaultVocabulary")

  override def modelInstance: AmfObject = DefaultVocabulary()

  override def fields: List[Field] = List(IRI)
}


/**
  * Stores information about a CURIE prefix defined in the context
  */
object CuriePrefixModel extends DomainElementModel with WithIri  {

  override val `type`: List[ValueType] = List(Meta + "ContextCuriePrefix")

  val Alias =
    Field(Str, Meta + "contextAlias", ModelDoc(ModelVocabularies.Meta, "contextAlias", "lexical value of the alias in the context"))

  override def modelInstance: AmfObject = CuriePrefix()

  override def fields: List[Field] = List(Alias, IRI)
}


/**
  * Stores information about mapping rules for a property in the model
  */
object ContextMappingModel extends DomainElementModel with WithIri {
  override val `type`: List[ValueType] = List(Meta + "ContextMapping")

  val Alias =
    Field(Str, Meta + "contextAlias", ModelDoc(ModelVocabularies.Meta, "contextAlias", "lexical value of the alias in the context"))

  val Coercion =
    Field(Str, Meta + "coercion", ModelDoc(ModelVocabularies.Meta, "coercion", "Type to coerce the mapped model"))

  val Nulled =
    Field(Bool, Meta + "null", ModelDoc(ModelVocabularies.Meta, "null", "Marks the baseIRI as null, preventing generation of absolute IRIs in the model"))

  override def fields: List[Field] = List(Alias, IRI, Coercion, Nulled)

  override def modelInstance: AmfObject = ContextMapping()
}


/**
  * Set of semantic contextual information that can be attached to a schema
  */
object SemanticContextModel extends DomainElementModel {
  override val `type`: List[ValueType] = List(Meta + "SemanticContext")

  val Base =
    Field(BaseIRIModel, Meta + "base", ModelDoc(ModelVocabularies.Meta, "base", "Base IRI used to generate all the @ids in the model"))

  val Vocab =
    Field(DefaultVocabularyModel, Meta + "vocab", ModelDoc(ModelVocabularies.Meta, "vocab", "Default IRI prefix used to map by default all properties and terms in the model"))

  val Curies =
    Field(Array(CuriePrefixModel), Meta + "curies", ModelDoc(ModelVocabularies.Meta, "curies", "Set of CURIE prefixes defined in a context"))

  val Mapping =
    Field(Array(ContextMappingModel), Meta + "mappings", ModelDoc(ModelVocabularies.Meta, "mappings", "Set of property mappings and coercions defined in a context"))

  val TypeMapping =
    Field(Array(Iri), Meta + "typeMapping", ModelDoc(ModelVocabularies.Meta, "typeMapping", "Set of types for a particular node"))

  override def fields: List[Field] = List(Base, Vocab, Curies, Mapping)

  override def modelInstance: AmfObject = SemanticContext()

}

trait WithSemanticContext {
  val Semantics =
    Field(SemanticContextModel, Meta + "semantics", ModelDoc(ModelVocabularies.Meta, "semantics", "Associated context model for domain element holding a schema"))
}