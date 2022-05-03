package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Encoding
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Str}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

/** Encoding metamodel.
  */
object EncodingModel extends DomainElementModel with KeyField {

  val PropertyName =
    Field(Str, ApiContract + "propertyName", ModelDoc(ModelVocabularies.ApiContract, "propertyName", ""))

  val ContentType =
    Field(Str, ApiContract + "contentType", ModelDoc(ModelVocabularies.ApiContract, "contentType", ""))

  val Headers =
    Field(Array(ParameterModel), ApiContract + "header", ModelDoc(ModelVocabularies.ApiContract, "header", ""))

  val Style = Field(
    Str,
    ApiContract + "style",
    ModelDoc(
      ModelVocabularies.ApiContract,
      "style",
      "Describes how a specific property value will be serialized depending on its type."
    )
  )

  val Explode = Field(Bool, ApiContract + "explode", ModelDoc(ModelVocabularies.ApiContract, "explode", ""))

  val AllowReserved =
    Field(Bool, ApiContract + "allowReserved", ModelDoc(ModelVocabularies.ApiContract, "allowReserved", ""))

  override val `type`: List[ValueType] = ApiContract + "Encoding" :: DomainElementModel.`type`

  override def fields: List[Field] =
    PropertyName :: ContentType :: Headers :: Style :: Explode :: AllowReserved :: DomainElementModel.fields

  override def modelInstance = Encoding()

  override val key: Field = PropertyName

  // TODO: doc, describe this model
  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Encoding",
    ""
  )
}
