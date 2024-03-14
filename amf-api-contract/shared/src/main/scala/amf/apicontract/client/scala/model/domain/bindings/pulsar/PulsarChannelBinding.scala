package amf.apicontract.client.scala.model.domain.bindings.pulsar

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.internal.metamodel.domain.bindings.PulsarChannelBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.{PulsarChannelBindingModel, PulsarChannelRetentionModel}
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Pulsar
import amf.core.client.scala.model.{BoolField, IntField, StrField}
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class PulsarChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field = BindingVersion
  override def meta: PulsarChannelBindingModel.type = PulsarChannelBindingModel

  def namespace: StrField               = fields.field(Namespace)
  def persistence: StrField             = fields.field(Persistence)
  def compaction: IntField              = fields.field(Compaction)
  def geoReplication: Seq[StrField]     = fields.field(GeoReplication)
  def retention: PulsarChannelRetention = fields.field(Retention)
  def ttl: IntField                     = fields.field(Ttl)
  def deduplication: BoolField          = fields.field(Deduplication)

  def withNamespace(namespace: String): this.type                 = set(Namespace, namespace)
  def withPersistence(persistence: String): this.type             = set(Persistence, persistence)
  def withCompaction(compaction: Integer): this.type              = set(Compaction, compaction)
  def withGeoReplication(geoReplication: Seq[String]): this.type  = set(GeoReplication, geoReplication)
  def withRetention(retention: PulsarChannelRetention): this.type = set(Retention, retention)
  def withTtl(ttl: Integer): this.type                            = set(Ttl, ttl)
  def withDeduplication(deduplication: Boolean): this.type        = set(Deduplication, deduplication)

  override def key: StrField = fields.field(PulsarChannelBindingModel.key)

  override def componentId: String              = s"/$Pulsar-operation"
  override def linkCopy(): PulsarChannelBinding = PulsarChannelBinding().withId(id)

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    PulsarChannelBinding.apply
}

object PulsarChannelBinding {

  def apply(): PulsarChannelBinding = apply(Annotations())

  def apply(annotations: Annotations): PulsarChannelBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): PulsarChannelBinding =
    new PulsarChannelBinding(fields, annotations)
}

class PulsarChannelRetention(override val fields: Fields, override val annotations: Annotations) extends DomainElement {
  override def meta: PulsarChannelRetentionModel.type = PulsarChannelRetentionModel

  def time: IntField = fields.field(PulsarChannelRetentionModel.Time)
  def size: IntField = fields.field(PulsarChannelRetentionModel.Size)

  def withTime(time: Integer): this.type = set(PulsarChannelRetentionModel.Time, time)
  def withSize(size: Integer): this.type = set(PulsarChannelRetentionModel.Size, size)

  override def componentId: String = s"/$Pulsar-retention"
}

object PulsarChannelRetention {

  def apply(): PulsarChannelRetention = apply(Annotations())

  def apply(annotations: Annotations): PulsarChannelRetention = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): PulsarChannelRetention =
    new PulsarChannelRetention(fields, annotations)
}
