package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.MessageBindings
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Array
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}

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
      Bindings
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = MessageBindings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MessageBindings",
    ""
  )
}
