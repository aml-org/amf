package amf.core.metamodel.document

import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.{Field, ModelDefaultBuilder, Obj}
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * Unit metamodel
  */
trait BaseUnitModel extends Obj with ModelDefaultBuilder {

  val Location = Field(Str, Document + "location")

  val References = Field(Array(BaseUnitModel), Document + "references")

  val Usage = Field(Str, Document + "usage")

}

object BaseUnitModel extends BaseUnitModel {

  override val `type`: List[ValueType] = List(Document + "Unit")

  override val fields: List[Field] = List(References, Usage)

  override def modelInstance = throw new Exception("BaseUnit is an abstract class")
}
