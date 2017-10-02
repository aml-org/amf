package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.Str
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.{Namespace, ValueType}

object NilShapeModel extends DomainElementModel {

  val Name = Field(Str, Namespace.Shacl + "name")

  override val fields: List[Field] = List(Name)

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "NilShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")
}
