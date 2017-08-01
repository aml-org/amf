package amf.metadata.domain

import amf.metadata.Type.Array
import amf.metadata.{Field, Obj, SourceMapModel}
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Domain element metamodel
  */
trait DomainElementModel extends Obj {

  val Extends = Field(Array(DomainElementModel), Document + "extends")

  val Includes = Field(Array(DomainElementModel), Document + "includes")

  val Sources = Field(SourceMapModel, Document + "sources")
}

object DomainElementModel extends DomainElementModel {
  override val `type`: List[ValueType] = List(Document + "DomainElement")

  override val fields: List[Field] = List(Extends, Includes)
}
