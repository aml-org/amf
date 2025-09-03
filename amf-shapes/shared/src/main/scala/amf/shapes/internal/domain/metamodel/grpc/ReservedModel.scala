package amf.shapes.internal.domain.metamodel.grpc

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Int, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.grpc.Reserved

trait ReservedModel extends DomainElementModel {

  val Range: Field = Field(
    ReservedRangeModel,
    Shapes + "range",
    ModelDoc(ModelVocabularies.Shapes, "range", "(GRPC) range of values to be reserved")
  )

  val FieldName: Field = Field(
    Str,
    Shapes + "fieldName",
    ModelDoc(ModelVocabularies.Shapes, "fieldName", "(GRPC) field name to be reserved")
  )

  val Number: Field = Field(
    Int,
    Shapes + "number",
    ModelDoc(ModelVocabularies.Shapes, "number", "(GRPC) number to be reserved")
  )

  override val `type`: List[ValueType] =
    List(Shapes + "Reserved") ++ DomainElementModel.`type`

  override def modelInstance: Reserved = Reserved()
}

object ReservedModel extends ReservedModel {
  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Reserved",
    "(GRPC) class to allocate values or fields to be reserved"
  )

  override val fields: List[Field] =
    DomainElementModel.fields ++ List(
      Range,
      FieldName,
      Number
    )
}
