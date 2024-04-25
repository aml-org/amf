package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.kafka._
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Int, Str, Array, Bool}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies, ShapeModel}

object KafkaOperationBindingModel extends OperationBindingModel with BindingVersion {
  val GroupId: Field =
    Field(
      ShapeModel,
      ApiBinding + "groupId",
      ModelDoc(ModelVocabularies.ApiBinding, "groupId", "Schema that defines the id of the consumer group")
    )

  val ClientId: Field =
    Field(
      ShapeModel,
      ApiBinding + "clientId",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "clientId",
        "Schema that defines the id of the consumer inside a consumer group"
      )
    )

  override def modelInstance: AmfObject = KafkaOperationBinding()

  override def fields: List[Field] = List(GroupId, ClientId, BindingVersion) ++ OperationBindingModel.fields

  override val key: Field = Type

  override val `type`: List[ValueType] = ApiBinding + "KafkaOperationBinding" :: OperationBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "KafkaOperationBinding")
}

trait KafkaMessageBindingModel extends MessageBindingModel with BindingVersion {
  val MessageKey: Field =
    Field(
      ShapeModel,
      ApiBinding + "messageKey",
      ModelDoc(ModelVocabularies.ApiBinding, "key", "Schema that defines the message key")
    )

  override def fields: List[Field] = List(MessageKey, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "KafkaMessageBinding" :: MessageBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "KafkaMessageBinding")
}

object KafkaMessageBindingModel extends KafkaMessageBindingModel {
  override def modelInstance: AmfObject = throw new Exception("KafkaMessageBindingModel is an abstract class")
}

object KafkaMessageBinding010Model extends KafkaMessageBindingModel {
  override def modelInstance: AmfObject = KafkaMessageBinding010()
  override val `type`: List[ValueType]  = ApiBinding + "KafkaMessageBinding010" :: MessageBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "KafkaMessageBinding010")
}

object KafkaMessageBinding030Model extends KafkaMessageBindingModel {
  val SchemaIdLocation: Field =
    Field(
      Str,
      ApiBinding + "schemaIdLocation",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "schemaIdLocation",
        "If a Schema Registry is used when performing this operation, tells where the id of schema is stored (e.g. header or payload)."
      )
    )

  val SchemaIdPayloadEncoding: Field =
    Field(
      Str,
      ApiBinding + "schemaIdPayloadEncoding",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "schemaIdPayloadEncoding",
        "Number of bytes or vendor specific values when schema id is encoded in payload (e.g confluent/ apicurio-legacy / apicurio-new)."
      )
    )

  val SchemaLookupStrategy: Field =
    Field(
      Str,
      ApiBinding + "schemaLookupStrategy",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "schemaLookupStrategy",
        "Freeform string for any naming strategy class to use. Clients should default to the vendor default if not supplied."
      )
    )

  override def modelInstance: AmfObject = KafkaMessageBinding030()

  override def fields: List[Field] = List(
    MessageKey,
    SchemaIdLocation,
    SchemaIdPayloadEncoding,
    SchemaLookupStrategy,
    BindingVersion
  ) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "KafkaMessageBinding030" :: MessageBindingModel.`type`

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "KafkaMessageBinding030")
}

// added in binding version 0.3.0
object KafkaServerBindingModel extends ServerBindingModel with BindingVersion {
  val SchemaRegistryUrl: Field =
    Field(
      Str,
      ApiBinding + "schemaRegistryUrl",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "schemaRegistryUrl",
        "API URL for the Schema Registry used when producing Kafka messages (if a Schema Registry was used)"
      )
    )

  // TODO: MUST NOT be specified if `schemaRegistryUrl` is not specified
  val SchemaRegistryVendor: Field =
    Field(
      Str,
      ApiBinding + "schemaRegistryVendor",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "schemaRegistryVendor",
        "The vendor of Schema Registry and Kafka serdes library that should be used"
      )
    )

  override def modelInstance: AmfObject = KafkaServerBinding()

  override def fields: List[Field] =
    List(SchemaRegistryUrl, SchemaRegistryVendor, BindingVersion) ++ ServerBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "KafkaServerBinding" :: ServerBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "KafkaServerBinding")
}

// added in binding version 0.3.0
trait KafkaChannelBindingModel extends ChannelBindingModel with BindingVersion {
  val Topic: Field =
    Field(
      Str,
      ApiBinding + "topic",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "topic",
        "Kafka topic name if different from channel name."
      )
    )

  val Partitions: Field =
    Field(
      Int,
      ApiBinding + "partitions",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "partitions",
        "Number of partitions configured on this topic."
      )
    )

  val Replicas: Field =
    Field(
      Int,
      ApiBinding + "replicas",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "replicas",
        "Number of replicas configured on this topic."
      )
    )

  override def fields: List[Field] = List(Topic, Partitions, Replicas, BindingVersion) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "KafkaChannelBinding" :: ChannelBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "KafkaChannelBinding")
}

object KafkaChannelBindingModel extends KafkaChannelBindingModel {
  override def modelInstance: AmfObject = throw new Exception("KafkaChannelBindingModel is an abstract class")
}

object KafkaChannelBinding030Model extends KafkaChannelBindingModel {
  override def modelInstance: AmfObject = KafkaChannelBinding030()
  override val `type`: List[ValueType]  = ApiBinding + "KafkaChannelBinding030" :: ChannelBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "KafkaChannelBinding030")
}

object KafkaChannelBinding040Model extends KafkaChannelBindingModel {
  override def modelInstance: AmfObject = KafkaChannelBinding040()
  override val `type`: List[ValueType]  = ApiBinding + "KafkaChannelBinding040" :: ChannelBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "KafkaChannelBinding040")

  val TopicConfiguration: Field = Field(
    KafkaTopicConfiguration040Model,
    ApiBinding + "topicConfiguration040",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "topicConfiguration040",
      "Topic configuration properties that are relevant for the API."
    )
  )

  override def fields: List[Field] =
    List(Topic, Partitions, Replicas, TopicConfiguration, BindingVersion) ++ ChannelBindingModel.fields
}

object KafkaChannelBinding050Model extends KafkaChannelBindingModel {
  override def modelInstance: AmfObject = KafkaChannelBinding050()
  override val `type`: List[ValueType]  = ApiBinding + "KafkaChannelBinding050" :: ChannelBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "KafkaChannelBinding050")

  val TopicConfiguration: Field = Field(
    KafkaTopicConfiguration050Model,
    ApiBinding + "topicConfiguration050",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "topicConfiguration050",
      "Topic configuration properties that are relevant for the API."
    )
  )

  override def fields: List[Field] =
    List(Topic, Partitions, Replicas, TopicConfiguration, BindingVersion) ++ ChannelBindingModel.fields
}

trait KafkaTopicConfigurationModel extends DomainElementModel {
  override val `type`: List[ValueType] = ApiBinding + "KafkaTopicConfiguration" :: DomainElementModel.`type`
  override val doc: ModelDoc           = ModelDoc(ModelVocabularies.ApiBinding, "KafkaTopicConfiguration")

  val CleanupPolicy: Field = Field(
    Array(Str),
    ApiBinding + "cleanup.policy",
    ModelDoc(ModelVocabularies.ApiBinding, "cleanup.policy", "The cleanup.policy configuration option.")
  )

  val RetentionMs: Field = Field(
    Int,
    ApiBinding + "retention.ms",
    ModelDoc(ModelVocabularies.ApiBinding, "retention.ms", "The retention.ms configuration option.")
  )

  val RetentionBytes: Field = Field(
    Int,
    ApiBinding + "retention.bytes",
    ModelDoc(ModelVocabularies.ApiBinding, "retention.bytes", "The retention.bytes configuration option.")
  )

  val DeleteRetentionMs: Field = Field(
    Int,
    ApiBinding + "delete.retention.ms",
    ModelDoc(ModelVocabularies.ApiBinding, "delete.retention.ms", "The delete.retention.ms configuration option.")
  )

  val MaxMessageBytes: Field = Field(
    Int,
    ApiBinding + "max.message.bytes",
    ModelDoc(ModelVocabularies.ApiBinding, "max.message.bytes", "The max.message.bytes configuration option.")
  )

  override def fields: List[Field] = List(
    CleanupPolicy,
    RetentionMs,
    RetentionBytes,
    DeleteRetentionMs,
    MaxMessageBytes
  )
}

object KafkaTopicConfigurationModel extends KafkaTopicConfigurationModel {
  override def modelInstance: AmfObject = throw new Exception("KafkaTopicConfigurationModel is an abstract class")
}

object KafkaTopicConfiguration040Model extends KafkaTopicConfigurationModel {
  override def modelInstance: AmfObject = KafkaTopicConfiguration040()
  override val `type`: List[ValueType]  = ApiBinding + "KafkaTopicConfiguration040" :: DomainElementModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "KafkaTopicConfiguration040")
}

object KafkaTopicConfiguration050Model extends KafkaTopicConfigurationModel {
  override def modelInstance: AmfObject = KafkaTopicConfiguration050()
  override val `type`: List[ValueType]  = ApiBinding + "KafkaTopicConfiguration050" :: DomainElementModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "KafkaTopicConfiguration050")

  val ConfluentKeySchemaValidation: Field = Field(
    Bool,
    ApiBinding + "confluent.key.schema.validation",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "confluent.key.schema.validation",
      "It shows whether the schema validation for the message key is enabled. Vendor specific config."
    )
  )

  val ConfluentKeySubjectNameStrategy: Field = Field(
    Str,
    ApiBinding + "confluent.key.subject.name.strategy",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "confluent.key.subject.name.strategy",
      "The name of the schema lookup strategy for the message key. Vendor specific config."
    )
  )

  val ConfluentValueSchemaValidation: Field = Field(
    Bool,
    ApiBinding + "confluent.value.schema.validation",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "confluent.value.schema.validation",
      "It shows whether the schema validation for the message value is enabled. Vendor specific config."
    )
  )

  val ConfluentValueSubjectNameStrategy: Field = Field(
    Str,
    ApiBinding + "confluent.value.subject.name.strategy",
    ModelDoc(
      ModelVocabularies.ApiBinding,
      "confluent.value.subject.name.strategy",
      "The name of the schema lookup strategy for the message value. Vendor specific config."
    )
  )

  override def fields: List[Field] = List(
    CleanupPolicy,
    RetentionMs,
    RetentionBytes,
    DeleteRetentionMs,
    MaxMessageBytes,
    ConfluentKeySchemaValidation,
    ConfluentKeySubjectNameStrategy,
    ConfluentValueSchemaValidation,
    ConfluentValueSubjectNameStrategy
  )
}
