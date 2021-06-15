package amf.shapes.internal.domain.metamodel

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.client.scala.vocabulary.Namespace._
import amf.core.client.scala.vocabulary.ValueType
import amf.shapes.client.scala.domain.models.NilShape

object NilShapeModel extends AnyShapeModel {

  override val `type`: List[ValueType] =
    List(Shapes + "NilShape") ++ ShapeModel.`type`

  override val fields: List[Field] = AnyShapeModel.fields

  override def modelInstance = NilShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "NilShape",
    "Data shape representing the null/nil value in the input schema"
  )
}
