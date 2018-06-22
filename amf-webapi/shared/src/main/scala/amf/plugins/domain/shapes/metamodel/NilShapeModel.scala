package amf.plugins.domain.shapes.metamodel

import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.models.NilShape

object NilShapeModel extends AnyShapeModel {

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "NilShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = NilShape()
}
