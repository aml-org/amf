package amf.shapes.internal.domain.metamodel

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.shapes.client.scala.model.domain.UnionShape

object UnionShapeModel extends AnyShapeModel {

  val AnyOf = Field(Array(ShapeModel),
                    Namespace.Shapes + "anyOf",
                    ModelDoc(ModelVocabularies.Shapes, "anyOf", "Data shapes in the union"))

  val specificFields = List(AnyOf)

  override val fields: List[Field] = specificFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "UnionShape") ++ AnyShapeModel.`type`

  override def modelInstance = UnionShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "UnionShape",
    "Shape representing the union of many alternative data shapes"
  )
}
