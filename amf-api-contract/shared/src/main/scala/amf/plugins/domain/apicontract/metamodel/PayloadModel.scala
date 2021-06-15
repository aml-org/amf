package amf.plugins.domain.apicontract.metamodel

import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{
  DomainElementModel,
  LinkableElementModel,
  ModelDoc,
  ModelVocabularies,
  ShapeModel
}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}

import amf.plugins.domain.apicontract.models.Payload

/**
  * Payload metamodel.
  */
object PayloadModel
    extends DomainElementModel
    with KeyField
    with OptionalField
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

  val Encoding =
    Field(
      Array(EncodingModel),
      ApiContract + "encoding",
      ModelDoc(
        ModelVocabularies.ApiContract,
        "encoding",
        "An array of properties and its encoding information. The key, being the property name, must exist in the schema as a property"
      )
    )

  override val key: Field = MediaType

  override val `type`: List[ValueType] = ApiContract + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: MediaType :: SchemaMediaType :: Schema :: Examples :: Encoding :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = Payload()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Payload",
    "Encoded payload using certain media-type"
  )
}
