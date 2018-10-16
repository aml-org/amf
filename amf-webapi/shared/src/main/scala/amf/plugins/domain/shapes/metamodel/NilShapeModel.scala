package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.models.NilShape

object NilShapeModel extends AnyShapeModel {

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "NilShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = NilShape()

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Nil Shape",
    "Data shape representing the null/nil value in the input schema"
  )
}
