package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

object KafkaOperationBindingModel extends OperationBindingModel with BindingVersion {
  val GroupId =
    Field(Str, ApiContract + "groupId", ModelDoc(ModelVocabularies.ApiContract, "groupId", "Id of the consumer group"))

  val ClientId =
    Field(Str,
          ApiContract + "clientId",
          ModelDoc(ModelVocabularies.ApiContract, "clientId", "Id of the consumer inside a consumer group"))

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(GroupId, ClientId, BindingVersion) ++ OperationBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "KafkaOperationBinding" :: OperationBindingModel.`type`
}

object KafkaMessageBindingModel extends OperationBindingModel with BindingVersion {
  val Key =
    Field(Str, ApiContract + "key", ModelDoc(ModelVocabularies.ApiContract, "key", "The message key"))

  override def modelInstance: AmfObject = ???

  override def fields: List[Field] = List(Key, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiContract + "KafkaMessageBinding" :: MessageBindingModel.`type`
}
