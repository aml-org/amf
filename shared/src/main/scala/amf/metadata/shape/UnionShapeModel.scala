package amf.metadata.shape

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Array
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.{Namespace, ValueType}

object UnionShapeModel extends ShapeModel with DomainElementModel {

  val AnyOf = Field(Array(ShapeModel), Namespace.Shapes + "anyOf")

  override def fields: List[Field] = List(AnyOf) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "UnionShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

}
