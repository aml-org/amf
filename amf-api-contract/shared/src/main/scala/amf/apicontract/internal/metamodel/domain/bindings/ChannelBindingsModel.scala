package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.ChannelBindings
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}

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
      Bindings
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = ChannelBindings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "ChannelBindings",
    ""
  )
}
