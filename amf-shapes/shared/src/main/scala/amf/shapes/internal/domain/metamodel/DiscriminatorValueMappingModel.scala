package amf.shapes.internal.domain.metamodel
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.shapes.client.scala.model.domain.DiscriminatorValueMapping

object DiscriminatorValueMappingModel extends DomainElementModel {

  val DiscriminatorValue: Field = Field(
    Str,
    Shapes + "discriminatorValue",
    ModelDoc(
      ModelVocabularies.Shapes,
      "discriminatorValue",
      "Value given to a discriminator that identifies a target Shape"
    )
  )

  val DiscriminatorValueTarget: Field =
    Field(
      ShapeModel,
      Shapes + "discriminatorValueTarget",
      ModelDoc(ModelVocabularies.Shapes, "discriminatorValueTarget", "Target shape for a certain discriminator value")
    )

  override val `type`: List[ValueType] = Shapes + "DiscriminatorValueMapping" :: DomainElementModel.`type`

  override def fields: List[Field] = DiscriminatorValue :: DiscriminatorValueTarget :: DomainElementModel.fields

  override def modelInstance: DiscriminatorValueMapping = DiscriminatorValueMapping()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "DiscriminatorValueMapping",
    "Mapping that relates a certain discriminator value to a certain shape"
  )
}
