package amf.metadata.document

import amf.metadata.Type.Array
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Document

/**
  * Unit metamodel
  */
trait UnitModel extends Type {

  val References = Field(Array(UnitModel), Document, "references")

}

object UnitModel extends UnitModel
