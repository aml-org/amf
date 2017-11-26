package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.plugins.domain.shapes.models.UnionShape
import amf.core.vocabulary.{Namespace, ValueType}

object UnionShapeModel extends ShapeModel with DomainElementModel {

  val AnyOf = Field(Array(ShapeModel), Namespace.Shapes + "anyOf")

  override def fields: List[Field] = List(AnyOf) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "UnionShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = UnionShape()
}
