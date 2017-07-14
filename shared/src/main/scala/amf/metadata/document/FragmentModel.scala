package amf.metadata.document

import amf.metadata.Field
import amf.metadata.Type.Array
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Document

/**
  * Fragment metamodel
  */
trait FragmentModel extends UnitModel {

  val Encodes = Field(Array(DomainElementModel), Document, "encodes")

}

object FragmentModel extends FragmentModel
