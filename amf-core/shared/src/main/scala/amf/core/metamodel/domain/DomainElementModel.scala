package amf.core.metamodel.domain

import amf.core.metamodel.Type.Array
import amf.core.metamodel.document.SourceMapModel
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.{Field, ModelDefaultBuilder, Obj, domain}
import amf.core.vocabulary.Namespace.{Document, SourceMaps}
import amf.core.vocabulary.{Namespace, ValueType}

/**
  * Stores meta-data about the semantics of the domain element
  * @param displayName
  * @param description
  * @param superClasses
  */
case class ModelDoc(vocabulary: ModelVocabulary = ModelVocabularies.Parser, displayName: String = "", description: String = "", superClasses: Seq[String] = Nil)

case class ModelVocabulary(alias: String, base: String, usage: String, filename: String)

object ModelVocabularies {
  val Parser = ModelVocabulary("parser", Namespace.AmfParser.base, "Internal namespace", "")
  val AmlDoc = ModelVocabulary("doc", Namespace.Document.base, "Document Model vocabulary for AMF. The Document Model defines the basic modular units where domain descriptions can be encoded.", "aml_doc.yaml")
  val Http = ModelVocabulary("http", Namespace.Http.base, "HTTP APIs vocabulary for AMF", "http.yaml")
  val Shapes = ModelVocabulary("shapes", Namespace.Shapes.base, "Vocabulary defining data shapes, used as an extension to SHACL", "shapes.yaml")
  val Data = ModelVocabulary("data", Namespace.Data.base, "Vocabulary defining a default set of classes to map data structures composed of recursive records of fields,\nlike the ones used in JSON or YAML into a RDF graph.\nThey can be validated using data shapes.", "data_model.yaml")
  val Meta = ModelVocabulary("meta", Namespace.Meta.base, "Vocabulary containing meta-definitions", "meta.yaml")
  val Security = ModelVocabulary("security",Namespace.Security.base, "Vocabulary for HTTP security information", "security.yaml")

  val all: Seq[ModelVocabulary] = Seq(AmlDoc, Http, Shapes, Data, Security, Meta)
}

object ExternalModelVocabularies {
  val SchemaOrg = ModelVocabulary("schema-org", Namespace.Schema.base, "Schema.org vocabulary", "")
  val Shacl = ModelVocabulary("shacl", Namespace.Shacl.base, "SHACL vocabulary", "")
  val Rdfs = ModelVocabulary("rdfs", Namespace.Rdfs.base, "RDFS vocabulary", "")
  val Rdf = ModelVocabulary("rdf", Namespace.Rdf.base, "RDF vocabulary", "")
  val Hydra = ModelVocabulary("hydra", Namespace.Hydra.base, "Hydra vocabulary", "")
  val Owl = ModelVocabulary("owl", Namespace.Owl.base, "OWL2 vocabulary", "")

  val all: Seq[ModelVocabulary] = Seq(SchemaOrg, Shacl, Rdfs, Rdf, Hydra, Owl)
}


/**
  * Domain element meta-model
  *
  * Base class for any element describing a domain model. Domain Elements are encoded into fragments
  */
trait DomainElementModel extends Obj with ModelDefaultBuilder {

  /**
    * Entity that is going to be extended overlaying or adding additional information
    * The type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model.
    */
  lazy val Extends = Field(Array(DomainElementModel), Document + "extends", ModelDoc(ModelVocabularies.AmlDoc, "extends", "Entity that is going to be extended overlaying or adding additional information\nThe type of the relationship provide the semantics about thow the referenced and referencer elements must be combined when generating the domain model from the document model."))

  /**
    * Indicates that this parsing Unit has SourceMaps
    */
  val Sources = Field(SourceMapModel, SourceMaps + "sources", ModelDoc(ModelVocabularies.AmlDoc,"source", "Indicates that this parsing Unit has SourceMaps"))

  // This creates a cycle in the among DomainModels, triggering a classnotdef problem
  // I need lazy evaluation here.
  // It cannot even be defined in the list of fields below
  lazy val CustomDomainProperties = Field(Array(DomainExtensionModel), Document + "customDomainProperties", ModelDoc(ModelVocabularies.AmlDoc,"custom domain properties", "Extensions provided for a particular domain element."))

}

object DomainElementModel extends DomainElementModel {

  // 'Static' values, we know the element schema before parsing
  // If the domain element is dynamic, the value from the model,
  // not the meta-model, should be retrieved instead

  override val `type`: List[ValueType] = List(Document + "DomainElement")

  override def fields: List[Field] = List(Extends)

  override def modelInstance =  throw new Exception("DomainElement is an abstract class")

  override val doc : ModelDoc = ModelDoc(
    vocabulary = ModelVocabularies.AmlDoc,
    displayName = "Domain element",
    description = "Base class for any element describing a domain model. Domain Elements are encoded or declared into base units",
  )
}
