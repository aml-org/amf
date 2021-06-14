package amf.plugins.domain.apicontract.metamodel.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.plugins.domain.apicontract.models.bindings.ChannelBindings
import amf.core.internal.metamodel.Type.Array

object ChannelBindingsModel extends DomainElementModel with NameFieldSchema {

  override val `type`: List[ValueType] = ApiBinding + "ChannelBindings" :: DomainElementModel.`type`

  val Bindings = Field(
    Array(ChannelBindingModel),
    ApiBinding + "bindings",
    ModelDoc(ModelVocabularies.ApiBinding, "bindings", "List of channel bindings")
  )

  override def fields: List[Field] =
    List(
      Name,
      Bindings,
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = ChannelBindings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "ChannelBindings",
    ""
  )
}
