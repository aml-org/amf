package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.{ApiContract, Core, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.common.ExamplesField
import amf.plugins.domain.webapi.models.Payload

/**
  * Payload metamodel.
  */
object PayloadModel
    extends DomainElementModel
    with KeyField
    with OptionalField
    with NameFieldSchema
    with LinkableElementModel
    with ExamplesField {

  val MediaType = Field(Str,
                        Core + "mediaType",
                        ModelDoc(ModelVocabularies.Core, "media type", "Media types supported in the payload"))

  val Schema =
    Field(ShapeModel, Shapes + "schema", ModelDoc(ModelVocabularies.Shapes, "schema", "Schema associated to this payload"))

  val Encoding = Field(Array(EncodingModel), ApiContract + "encoding", ModelDoc(ModelVocabularies.ApiContract, "encoding", ""))

  override val key: Field = MediaType

  override val `type`: List[ValueType] = ApiContract + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: MediaType :: Schema :: Examples :: Encoding :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = Payload()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Payload",
    "Encoded payload using certain media-type"
  )
}
