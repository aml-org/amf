package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.kafka.{KafkaOperationBinding, KafkaMessageBinding}

object KafkaOperationBindingModel extends OperationBindingModel with BindingVersion {
  val GroupId =
    Field(Str, ApiBinding + "groupId", ModelDoc(ModelVocabularies.ApiBinding, "groupId", "Id of the consumer group"))

  val ClientId =
    Field(Str,
          ApiBinding + "clientId",
          ModelDoc(ModelVocabularies.ApiBinding, "clientId", "Id of the consumer inside a consumer group"))

  override def modelInstance: AmfObject = KafkaOperationBinding()

  override def fields: List[Field] = List(GroupId, ClientId, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "KafkaOperationBinding" :: OperationBindingModel.`type`
}

object KafkaMessageBindingModel extends OperationBindingModel with BindingVersion {
  val Key =
    Field(Str, ApiBinding + "key", ModelDoc(ModelVocabularies.ApiBinding, "key", "The message key"))

  override def modelInstance: AmfObject = KafkaMessageBinding()

  override def fields: List[Field] = List(Key, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "KafkaMessageBinding" :: MessageBindingModel.`type`
}
