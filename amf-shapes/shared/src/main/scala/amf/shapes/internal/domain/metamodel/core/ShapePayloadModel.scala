package amf.shapes.internal.domain.metamodel.core

import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.{
  DomainElementModel,
  LinkableElementModel,
  ModelDoc,
  ModelVocabularies,
  ShapeModel
}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.shapes.client.scala.model.domain.core.ShapePayload
import amf.shapes.internal.domain.metamodel.common.ExamplesField

object ShapePayloadModel
    extends DomainElementModel
    with KeyField
    with NameFieldSchema
    with DescriptionField
    with LinkableElementModel
    with ExamplesField {

  val MediaType = Field(Str,
                        Core + "mediaType",
                        ModelDoc(ModelVocabularies.Core, "mediaType", "Media types supported in the payload"))

  val SchemaMediaType = Field(
    Str,
    ApiContract + "schemaMediaType",
    ModelDoc(ModelVocabularies.ApiContract, "schemaMediaType", "Defines the format of the payload schema"))

  val Schema =
    Field(ShapeModel,
          Shapes + "schema",
          ModelDoc(ModelVocabularies.Shapes, "schema", "Schema associated to this payload"))

  override val key: Field = MediaType

  override val `type`: List[ValueType] = ApiContract + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: MediaType :: SchemaMediaType :: Schema :: Examples :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = ShapePayload()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Payload",
    "Encoded payload using certain media-type"
  )
}
