package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.googlepubsub._
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubChannelBindingModel.Type
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.common.NameFieldSchema

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
        "If non-empty, identifies related messages for which publish order should be respected"
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

trait GooglePubSubChannelBindingModel extends ChannelBindingModel with BindingVersion {
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
        "schemaSettings",
        "Settings for validating messages published against a schema"
      )
    )

  override def fields: List[Field] = List(
    Labels,
    MessageRetentionDuration,
    MessageStoragePolicy,
    SchemaSettings,
    BindingVersion
  ) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "GooglePubSubChannelBinding" :: ChannelBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubChannelBinding")
}

object GooglePubSubChannelBindingModel extends GooglePubSubChannelBindingModel {
  override def modelInstance: AmfObject = throw new Exception("GooglePubSubChannelBindingModel is an abstract class")
}

object GooglePubSubChannelBinding010Model extends GooglePubSubChannelBindingModel {
  override def modelInstance: AmfObject = GooglePubSubChannelBinding010()
  override val `type`: List[ValueType]  = ApiBinding + "GooglePubSubChannelBinding010" :: ChannelBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubChannelBinding010")

  val Topic: Field =
    Field(
      Str,
      ApiBinding + "topic",
      ModelDoc(ModelVocabularies.ApiBinding, "topic", "The Google Cloud Pub/Sub Topic name")
    )

  override def fields: List[Field] = List(Topic) ++ GooglePubSubChannelBindingModel.fields
}

object GooglePubSubChannelBinding020Model extends GooglePubSubChannelBindingModel {
  override def modelInstance: AmfObject = GooglePubSubChannelBinding020()
  override val `type`: List[ValueType]  = ApiBinding + "GooglePubSubChannelBinding020" :: ChannelBindingModel.`type`
  override val doc: ModelDoc            = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubChannelBinding020")
}

object GooglePubSubMessageStoragePolicyModel extends DomainElementModel {
  def modelInstance: AmfObject = GooglePubSubMessageStoragePolicy()
  val `type`: List[ValueType]  = ApiBinding + "GooglePubSubMessageStoragePolicy" :: DomainElementModel.`type`
  override val doc: ModelDoc   = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubMessageStoragePolicy")

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

  def fields: List[Field] = List(AllowedPersistenceRegions) ++ DomainElementModel.fields
}

object GooglePubSubSchemaSettingsModel extends DomainElementModel with NameFieldSchema {
  def modelInstance: AmfObject = GooglePubSubSchemaSettings()
  val `type`: List[ValueType]  = ApiBinding + "GooglePubSubSchemaSettings" :: DomainElementModel.`type`
  override val doc: ModelDoc   = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubSchemaSettings")

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

  def fields: List[Field] = List(Name, Encoding, FirstRevisionId, LastRevisionId) ++ DomainElementModel.fields
}

object GooglePubSubSchemaDefinitionModel extends DomainElementModel with NameFieldSchema {
  def modelInstance: AmfObject = GooglePubSubSchemaDefinition()
  val `type`: List[ValueType]  = ApiBinding + "GooglePubSubSchemaDefinition" :: DomainElementModel.`type`
  override val doc: ModelDoc   = ModelDoc(ModelVocabularies.ApiBinding, "GooglePubSubSchemaDefinition")

  val FieldType: Field =
    Field(
      Str,
      ApiBinding + "type",
      ModelDoc(ModelVocabularies.ApiBinding, "type", "The type of the schema")
    )

  def fields: List[Field] = List(Name, FieldType) ++ DomainElementModel.fields
}
