package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.ChannelBindings

object ChannelBindingsModel extends DomainElementModel with NameFieldSchema {

  override val `type`: List[ValueType] = ApiBinding + "ChannelBindings" :: DomainElementModel.`type`

  val Bindings = Field(
    Array(ChannelBindingModel),
    ApiBinding + "binding",
    ModelDoc(ModelVocabularies.ApiBinding, "binding", "List of channel bindings")
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
