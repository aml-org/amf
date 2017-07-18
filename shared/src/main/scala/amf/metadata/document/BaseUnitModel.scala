package amf.metadata.document

import amf.metadata.Type.Array
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Unit metamodel
  */
trait BaseUnitModel extends Type {

  val References = Field(Array(BaseUnitModel), Document + "references")

  override val `type`: List[ValueType] = List(Document + "Unit")
}

object BaseUnitModel extends BaseUnitModel
