package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Meta
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Iri, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain._

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
    Field(Bool,
          Meta + "nulled",
          ModelDoc(ModelVocabularies.Meta,
                   "nulled",
                   "Marks the baseIRI as null, preventing generation of absolute IRIs in the model"))

  override def modelInstance: AmfObject = BaseIri()

  override def fields: List[Field] = List(IRI, Nulled)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "baseIRIModel",
    "Encodes information about the base document IRI for the model element @ids"
  )
}

/**
  * Encodes information about the base vocabulary to map by default properties and types in the model
  */
object DefaultVocabularyModel extends DomainElementModel with WithIri {

  override val `type`: List[ValueType] = List(Meta + "ContextDefaultVocabulary")

  override def modelInstance: AmfObject = DefaultVocabulary()

  override def fields: List[Field] = List(IRI)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "defaultVocabularyModel",
    "Encodes information about the base vocabulary to map by default properties and types in the model"
  )
}

/**
  * Stores information about a CURIE prefix defined in the context
  */
object CuriePrefixModel extends DomainElementModel with WithIri {

  override val `type`: List[ValueType] = List(Meta + "ContextCuriePrefix")

  val Alias =
    Field(Str,
          Meta + "contextAlias",
          ModelDoc(ModelVocabularies.Meta, "contextAlias", "lexical value of the alias in the context"))

  override def modelInstance: AmfObject = CuriePrefix()

  override def fields: List[Field] = List(Alias, IRI)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "curiePrefixModel",
    "Stores information about a CURIE prefix defined in the context"
  )
}

/**
  * Stores information about mapping rules for a property in the model
  */
object ContextMappingModel extends DomainElementModel with WithIri {
  override val `type`: List[ValueType] = List(Meta + "ContextMapping")

  val Alias =
    Field(Str,
          Meta + "contextAlias",
          ModelDoc(ModelVocabularies.Meta, "contextAlias", "lexical value of the alias in the context"))

  val Coercion =
    Field(Str, Meta + "coercion", ModelDoc(ModelVocabularies.Meta, "coercion", "Type to coerce the mapped model"))

  val Nulled =
    Field(Bool,
          Meta + "nulled",
          ModelDoc(ModelVocabularies.Meta,
                   "nulled",
                   "Marks the baseIRI as null, preventing generation of absolute IRIs in the model"))

  override def fields: List[Field] = List(Alias, IRI, Coercion, Nulled)

  override def modelInstance: AmfObject = ContextMapping()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "contextMappingModel",
    "Stores information about mapping rules for a property in the model"
  )
}

/**
  * Set of semantic contextual information that can be attached to a schema
  */
object SemanticContextModel extends DomainElementModel {
  override val `type`: List[ValueType] = List(Meta + "SemanticContext")

  val Base =
    Field(BaseIRIModel,
          Meta + "base",
          ModelDoc(ModelVocabularies.Meta, "base", "Base IRI used to generate all the @ids in the model"))

  val Vocab =
    Field(
      DefaultVocabularyModel,
      Meta + "vocab",
      ModelDoc(ModelVocabularies.Meta,
               "vocab",
               "Default IRI prefix used to map by default all properties and terms in the model")
    )

  val Curies =
    Field(Array(CuriePrefixModel),
          Meta + "curies",
          ModelDoc(ModelVocabularies.Meta, "curies", "Set of CURIE prefixes defined in a context"))

  val Mapping =
    Field(Array(ContextMappingModel),
          Meta + "mappings",
          ModelDoc(ModelVocabularies.Meta, "mappings", "Set of property mappings and coercions defined in a context"))

  val TypeMapping =
    Field(Array(Iri),
          Meta + "typeMapping",
          ModelDoc(ModelVocabularies.Meta, "typeMapping", "Set of types for a particular node"))

  override def fields: List[Field] = List(Base, Vocab, Curies, Mapping)

  override def modelInstance: AmfObject = SemanticContext()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Meta,
    "semanticContextModel",
    "Set of semantic contextual information that can be attached to a schema"
  )
}

trait WithSemanticContext {
  val Semantics =
    Field(
      SemanticContextModel,
      Meta + "semantics",
      ModelDoc(ModelVocabularies.Meta, "semantics", "Associated context model for domain element holding a schema"))
}
