package amf.core.metamodel.document

import amf.core.metamodel.Type.{Array, Iri, Str}
import amf.core.metamodel.{Field, ModelDefaultBuilder, Obj}
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.{Namespace, ValueType}

/**
  * BaseUnit metamodel
  *
  * Base class for every single document model unit. After parsing a document the parser generate parsing Base Units.
  * Base Units encode the domain elements and can reference other units to re-use descriptions.
  */
trait BaseUnitModel extends Obj with ModelDefaultBuilder {

  val Location = Field(Str, Document + "location")

  val References = Field(Array(BaseUnitModel), Document + "references")

  val Usage = Field(Str, Document + "usage")

  val DescribedBy=Field(Iri,ValueType(Namespace.Meta,"describedBy"),true)

}

object BaseUnitModel extends BaseUnitModel {

  override val `type`: List[ValueType] = List(Document + "Unit")

  override val fields: List[Field] = List(References, Usage,DescribedBy)

  override def modelInstance = throw new Exception("BaseUnit is an abstract class")
}
