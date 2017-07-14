package amf.metadata.domain

import amf.metadata.Type.Array
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Document

/**
  * Created by pedro.colunga on 7/14/17.
  */
trait DomainElementModel extends Type {

  val Extends = Field(Array(DomainElementModel), Document, "extends")

  val Includes = Field(Array(DomainElementModel), Document, "includes")

}

object DomainElementModel extends DomainElementModel
