package amf.plugins.domain.shapes.metamodel
import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.core.vocabulary.Namespace.Shapes
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.DiscriminatorValueMapping

object DiscriminatorValueMappingModel extends DomainElementModel {

  val DiscriminatorValue: Field = Field(
    Str,
    Shapes + "discriminatorValue",
    ModelDoc(ModelVocabularies.Shapes,
             "discriminatorValue",
             "Value given to a discriminator that identifies a target Shape")
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
