package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.pulsar.{
  PulsarChannelBinding,
  PulsarChannelRetention,
  PulsarServerBinding
}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Int, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

object PulsarServerBindingModel extends ServerBindingModel with BindingVersion {
  val Tenant: Field =
    Field(
      Str,
      ApiBinding + "tenant",
      ModelDoc(ModelVocabularies.ApiBinding, "tenant", "The pulsar tenant. If omitted, \"public\" MUST be assumed.")
    )

  override def modelInstance: AmfObject = PulsarServerBinding()

  override def fields: List[Field] = List(Tenant, BindingVersion) ++ ServerBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "PulsarServerBinding" :: ServerBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "PulsarServerBinding")
}

object PulsarChannelBindingModel extends ChannelBindingModel with BindingVersion {
  val Namespace: Field =
    Field(
      Str,
      ApiBinding + "namespace",
      ModelDoc(ModelVocabularies.ApiBinding, "namespace", "The namespace the channel is associated with.")
    )

  val Persistence: Field =
    Field(
      Str,
      ApiBinding + "persistence",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "persistence",
        "Persistence of the topic in Pulsar. It MUST be either persistent or non-persistent."
      )
    )

  val Compaction: Field =
    Field(
      Int,
      ApiBinding + "compaction",
      ModelDoc(ModelVocabularies.ApiBinding, "compaction", "Topic compaction threshold given in Megabytes.")
    )

  val GeoReplication: Field =
    Field(
      Array(Str),
      ApiBinding + "geo-replication",
      ModelDoc(ModelVocabularies.ApiBinding, "geo-replication", "A list of clusters the topic is replicated to.")
    )

  val Retention: Field =
    Field(
      PulsarChannelRetentionModel,
      ApiBinding + "retention",
      ModelDoc(ModelVocabularies.ApiBinding, "retention", "Topic retention policy.")
    )

  val Ttl: Field =
    Field(
      Int,
      ApiBinding + "ttl",
      ModelDoc(ModelVocabularies.ApiBinding, "ttl", "Message time-to-live in seconds.")
    )

  val Deduplication: Field =
    Field(
      Bool,
      ApiBinding + "deduplication",
      ModelDoc(
        ModelVocabularies.ApiBinding,
        "deduplication",
        "Message deduplication. When true, it ensures that each message produced on Pulsar topics is persisted to disk only once."
      )
    )

  override def modelInstance: AmfObject = PulsarChannelBinding()

  override def fields: List[Field] = List(
    Namespace,
    Persistence,
    Compaction,
    GeoReplication,
    Retention,
    Ttl,
    Deduplication,
    BindingVersion
  ) ++ ChannelBindingModel.fields

  override val `type`: List[ValueType] = ApiBinding + "PulsarChannelBinding" :: ChannelBindingModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "PulsarChannelBinding")
}

object PulsarChannelRetentionModel extends DomainElementModel {

  val Time: Field =
    Field(
      Int,
      ApiBinding + "time",
      ModelDoc(ModelVocabularies.ApiBinding, "time", "Time given in Minutes. Defaults to 0")
    )

  val Size: Field =
    Field(
      Int,
      ApiBinding + "size",
      ModelDoc(ModelVocabularies.ApiBinding, "size", "Size given in MegaBytes. Defaults to 0")
    )

  override def fields: List[Field] = List(Time, Size) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "PulsarChannelRetention" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = PulsarChannelRetention()

  override val doc: ModelDoc = ModelDoc(ModelVocabularies.ApiBinding, "PulsarChannelRetention")
}
