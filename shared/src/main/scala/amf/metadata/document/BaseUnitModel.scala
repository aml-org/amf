package amf.metadata.document

import amf.metadata.Type.Array
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Document

/**
  * Unit metamodel
  */
trait BaseUnitModel extends Type {

  val References = Field(Array(BaseUnitModel), Document, "references")

}

object BaseUnitModel extends BaseUnitModel
