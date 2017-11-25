package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.AnyShape
import amf.core.vocabulary.{Namespace, ValueType}

object AnyShapeModel extends DomainElementModel {
  val Name = Field(Str, Namespace.Shacl + "name")

  override def fields: List[Field] = List(Name)

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "AnyShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = AnyShape()
}
