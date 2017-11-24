package amf.plugins.domain.shapes.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.NilShape
import amf.framework.vocabulary.{Namespace, ValueType}

object NilShapeModel extends DomainElementModel {

  val Name = Field(Str, Namespace.Shacl + "name")

  override val fields: List[Field] = List(Name)

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "NilShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = NilShape()
}
