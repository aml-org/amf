package amf.metadata.domain

import amf.metadata.Type.Array
import amf.metadata.domain.extensions.DomainExtensionModel
import amf.metadata.{Field, Obj, SourceMapModel}
import amf.vocabulary.Namespace.{Document, SourceMaps}
import amf.vocabulary.ValueType

/**
  * Domain element meta-model
  */
trait DomainElementModel extends Obj {

  val Extends = Field(Array(DomainElementModel), Document + "extends")

  val Includes = Field(Array(DomainElementModel), Document + "includes")

  val Sources = Field(SourceMapModel, SourceMaps + "sources")

  // This creates a cycle in the among DomainModels, triggering a classnotdef problem
  // I need lazy evaluation here.
  // It cannot even be defined in the list of fields below
  lazy val CustomDomainProperties = Field(Array(DomainExtensionModel), Document + "customDomainProperties")

  // This can be override by dynamic element models to provide
  // the information about fields at parsing time

  val dynamic: Boolean = false
}

object DomainElementModel extends DomainElementModel {

  // 'Static' values, we know the element schema before parsing
  // If the domain element is dynamic, the value from the model,
  // not the meta-model, should be retrieved instead

  override val `type`: List[ValueType] = List(Document + "DomainElement")

  override val fields: List[Field] = List(Extends, Includes)

}
