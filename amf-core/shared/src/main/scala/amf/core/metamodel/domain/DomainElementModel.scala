package amf.core.metamodel.domain

import amf.core.metamodel.Type.Array
import amf.core.metamodel.document.SourceMapModel
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.{Field, ModelDefaultBuilder, Obj}
import amf.core.vocabulary.Namespace.{Document, SourceMaps}
import amf.core.vocabulary.ValueType

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
  lazy val Extends = Field(Array(DomainElementModel), Document + "extends")

  /**
    * Indicates that this parsing Unit has SourceMaps
    */
  val Sources = Field(SourceMapModel, SourceMaps + "sources")

  // This creates a cycle in the among DomainModels, triggering a classnotdef problem
  // I need lazy evaluation here.
  // It cannot even be defined in the list of fields below
  lazy val CustomDomainProperties = Field(Array(DomainExtensionModel), Document + "customDomainProperties")

}

object DomainElementModel extends DomainElementModel {

  // 'Static' values, we know the element schema before parsing
  // If the domain element is dynamic, the value from the model,
  // not the meta-model, should be retrieved instead

  override val `type`: List[ValueType] = List(Document + "DomainElement")

  override def fields: List[Field] = List(Extends)

  override def modelInstance =  throw new Exception("DomainElement is an abstract class")
}
