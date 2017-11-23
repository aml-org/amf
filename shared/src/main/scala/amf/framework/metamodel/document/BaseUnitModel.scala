package amf.framework.metamodel.document

import amf.framework.metamodel.Type.{Array, Str}
import amf.framework.metamodel.{Field, ModelDefaultBuilder, Obj}
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

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
