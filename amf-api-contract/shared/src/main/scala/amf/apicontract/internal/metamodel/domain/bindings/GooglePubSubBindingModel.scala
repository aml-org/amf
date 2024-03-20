package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{GooglePubSubChannelBinding, GooglePubSubMessageBinding, GooglePubSubMessageStoragePolicy, GooglePubSubSchemaDefinition, GooglePubSubSchemaSettings}
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubChannelBindingModel.Type
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain._

object GooglePubSubMessageBindingModel extends MessageBindingModel with BindingVersion {

  val Attributes: Field =
    Field(
      ObjectNodeModel,
      ApiBinding + "attribute",
      ModelDoc(ModelVocabularies.ApiBinding, "attributes", "Attributes for this message")
    )

  val OrderingKey: Field =
    Field(
      Str,
      ApiBinding + "orderingKey",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "orderingKey",
        "identifies related messages for which publish order should be respected"
      )
    )

  val Schema: Field =
    Field(
      GooglePubSubSchemaDefinitionModel,
      ApiBinding + "schemaDefinition",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "schema",
        "Define Schema"
      )
    )

  override def modelInstance: AmfObject = GooglePubSubMessageBinding()

  override def fields: List[Field] = List(Attributes, OrderingKey, Schema, BindingVersion) ++ MessageBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "GooglePubSubMessageBinding" :: MessageBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubMessageBinding")
}
object GooglePubSubChannelBindingModel extends ChannelBindingModel with BindingVersion {
  val Labels: Field =
    Field(
      ObjectNodeModel,
      ApiBinding + "labels",
      ModelDoc(ModelVocabularies.ApiBinding, "labels", "An object of key-value pairs ")
    )

  val MessageRetentionDuration: Field =
    Field(
      Str,
      ApiBinding + "messageRetentionDuration",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "messageRetentionDuration",
        "Indicates the minimum duration to retain a message after it is published to the topic"
      )
    )

  val MessageStoragePolicy: Field =
    Field(
      GooglePubSubMessageStoragePolicyModel,
      ApiBinding + "messageStoragePolicy",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "messageStoragePolicy",
        "Policy constraining the set of Google Cloud Platform regions where messages published to the topic may be stored"
      )
    )

  val SchemaSettings: Field =
    Field(
      GooglePubSubSchemaSettingsModel,
      ApiBinding + "schemaSettings",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "messageStoragePolicy",
        "Policy constraining the set of Google Cloud Platform regions where messages published to the topic may be stored"
      )
    )

  val Topic: Field =
    Field(
      Str,
      ApiBinding + "topic",
      ModelDoc(ModelVocabularies.ApiBinding, "topic", "The Google Cloud Pub/Sub Topic name")
    )

  override def modelInstance: AmfObject = GooglePubSubChannelBinding()

  override def fields: List[Field] = List(
    Labels,
    MessageRetentionDuration,
    MessageStoragePolicy,
    SchemaSettings,
    Topic,
    BindingVersion
  ) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "GooglePubSubChannelBinding" :: ChannelBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubChannelBinding")
}
object GooglePubSubMessageStoragePolicyModel extends DomainElementModel {

  val AllowedPersistenceRegions: Field =
    Field(
      Array(Str),
      ApiBinding + "allowedPersistenceRegions",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "allowedPersistenceRegions",
        "A list of IDs of GCP regions where messages that are published to the topic may be persisted in storage"
      )
    )
  def modelInstance: AmfObject = GooglePubSubMessageStoragePolicy()

  def fields: List[Field] = List(AllowedPersistenceRegions) ++ DomainElementModel.fields

  val `type`: List[ValueType] = ApiBinding + "GooglePubSubMessageStoragePolicy" :: DomainElementModel.`type`

  val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubMessageStoragePolicy")

}
object GooglePubSubSchemaSettingsModel extends DomainElementModel {

  val Encoding: Field =
    Field(
      Str,
      ApiBinding + "encoding",
      ModelDoc(ModelVocabularies.ApiBinding, "encoding", "The encoding of the message ")
    )

  val FirstRevisionId: Field =
    Field(
      Str,
      ApiBinding + "firstRevisionId",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "firstRevisionId",
        "The minimum (inclusive) revision allowed for validating messages"
      )
    )

  val LastRevisionId: Field =
    Field(
      Str,
      ApiBinding + "lastRevisionId",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "lastRevisionId",
        "The maximum (inclusive) revision allowed for validating messages"
      )
    )

  val Name: Field =
    Field(
      Str,
      ApiBinding + "name",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "name",
        "TThe name of the schema that messages published should be validated against"
      )
    )

  def modelInstance: AmfObject = GooglePubSubSchemaSettings()

  def fields: List[Field] = List(Encoding, FirstRevisionId, LastRevisionId, Name) ++ DomainElementModel.fields

  val `type`: List[ValueType] = ApiBinding + "GooglePubSubSchemaSettings" :: DomainElementModel.`type`

  val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubSchemaSettings")

}
object GooglePubSubSchemaDefinitionModel extends DomainElementModel {
  val Name: Field =
    Field(
      Str,
      ApiBinding + "name",
      ModelDoc(ModelVocabularies.ApiBinding, "name", "The name of the schema")
    )

  val FieldType: Field =
    Field(
      Str,
      ApiBinding + "type",
      ModelDoc(ModelVocabularies.ApiBinding, "type", "The type of the schema")
    )

  def modelInstance: AmfObject = GooglePubSubSchemaDefinition()

  def fields: List[Field] = List(Name, FieldType) ++ DomainElementModel.fields

  val `type`: List[ValueType] = ApiBinding + "GooglePubSubSchemaDefinition" :: DomainElementModel.`type`

  val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubSchemaDefinition")

}
