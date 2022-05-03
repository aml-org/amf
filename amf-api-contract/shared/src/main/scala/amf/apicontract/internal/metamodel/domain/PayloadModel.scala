package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Payload
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.templates.OptionalField
import amf.shapes.internal.domain.metamodel.operations.AbstractPayloadModel

/** Payload metamodel.
  */
object PayloadModel extends AbstractPayloadModel with OptionalField with LinkableElementModel {
  val SchemaMediaType: Field = Field(
    Str,
    ApiContract + "schemaMediaType",
    ModelDoc(ModelVocabularies.ApiContract, "schemaMediaType", "Defines the format of the payload schema")
  )

  val Encoding: Field =
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

  override val `type`: List[ValueType] = ApiContract + "Payload" :: Core + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: MediaType :: SchemaMediaType :: Schema :: Examples :: Encoding :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance: AmfObject = Payload()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Payload",
    "Encoded payload using certain media-type"
  )
}
