package amf.metadata.document

import amf.metadata.Field
import amf.metadata.Type.Array
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document

/**
  * Module metamodel
  */
trait ModuleModel extends UnitModel {

  val Declares = Field(Array(DomainElementModel), Document, "declares")

}

object ModuleModel extends ModuleModel
