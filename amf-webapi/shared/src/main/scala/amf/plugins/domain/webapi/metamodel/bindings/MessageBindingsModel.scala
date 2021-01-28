package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.MessageBindings

object MessageBindingsModel extends DomainElementModel with NameFieldSchema {

  override val `type`: List[ValueType] = ApiBinding + "MessageBindings" :: DomainElementModel.`type`

  val Bindings = Field(
    Array(MessageBindingModel),
    ApiBinding + "bindings",
    ModelDoc(ModelVocabularies.ApiBinding, "bindings", "List of message bindings")
  )

  override def fields: List[Field] =
    List(
      Name,
      Bindings,
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = MessageBindings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MessageBindings",
    ""
  )
}
