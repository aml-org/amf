package amf.shapes.internal.domain.metamodel.grpc

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Int
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.grpc.ReservedRange

trait ReservedRangeModel extends DomainElementModel {

  val From: Field = Field(
    Int,
    Shapes + "from",
    ModelDoc(ModelVocabularies.Shapes, "from", "(GRPC) from value of the range")
  )

  val To: Field = Field(
    Int,
    Shapes + "to",
    ModelDoc(ModelVocabularies.Shapes, "to", "(GRPC) to value of the range")
  )

  override val `type`: List[ValueType] =
    List(Shapes + "ReservedRange") ++ DomainElementModel.`type`

  override def modelInstance: ReservedRange = ReservedRange()
}

object ReservedRangeModel extends ReservedRangeModel {

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ReservedRange",
    "(GRPC) class to allocate a reserved range of values"
  )

  override val fields: List[Field] =
    DomainElementModel.fields ++ List(
      From,
      To
    )
}
