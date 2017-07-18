package amf.metadata.domain

import amf.metadata.Type.Array
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Domain element metamodel
  */
trait DomainElementModel extends Type {

  val Extends = Field(Array(DomainElementModel), Document + "extends")

  val Includes = Field(Array(DomainElementModel), Document + "includes")
}

object DomainElementModel extends DomainElementModel {
  override val `type`: List[ValueType] = List(Document + "DomainElement")
}
