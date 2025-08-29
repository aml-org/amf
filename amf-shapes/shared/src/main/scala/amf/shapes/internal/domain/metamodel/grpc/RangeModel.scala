package amf.shapes.internal.domain.metamodel.grpc

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Int
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.shapes.client.scala.model.domain.AnyShape

trait RangeModel extends DomainElementModel {

  val From: Field = Field(
    Int,
    Shapes + "from",
    ModelDoc(ModelVocabularies.Shapes, "from", "(GRPC) range of values to be reserved")
  )

  val To: Field = Field(
    Int,
    Shapes + "to",
    ModelDoc(ModelVocabularies.Shapes, "to", "(GRPC) field name to be reserved")
  )

  override val `type`: List[ValueType] =
    List(Shapes + "Range") ++ DomainElementModel.`type`

  override def modelInstance: AnyShape = AnyShape()
}

object RangeModel extends RangeModel {

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Reserved",
    "(GRPC) class to allocate values or fields to be reserved"
  )

  override val fields: List[Field] =
    DomainElementModel.fields ++ List(
      From,
      To
    )
}
