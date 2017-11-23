package amf.framework.metamodel.document

import amf.framework.metamodel.Type.{Str,Array}
import amf.framework.metamodel.{Field, Obj}
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Unit metamodel
  */
trait BaseUnitModel extends Obj {

  val Location = Field(Str, Document + "location")

  val References = Field(Array(BaseUnitModel), Document + "references")

  val Usage = Field(Str, Document + "usage")
}

object BaseUnitModel extends BaseUnitModel {

  override val `type`: List[ValueType] = List(Document + "Unit")

  override val fields: List[Field] = List(References, Usage)
}
