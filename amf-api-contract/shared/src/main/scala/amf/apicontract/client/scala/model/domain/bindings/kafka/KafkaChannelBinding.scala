package amf.apicontract.client.scala.model.domain.bindings.kafka

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.internal.metamodel.domain.bindings.KafkaChannelBindingModel
import amf.apicontract.internal.metamodel.domain.bindings.KafkaChannelBindingModel._
import amf.core.client.scala.model.{StrField, IntField}
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

class KafkaChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {

  def topic: StrField      = fields.field(Topic)
  def partitions: IntField = fields.field(Partitions)
  def replicas: IntField   = fields.field(Replicas)

  override def meta: KafkaChannelBindingModel.type = KafkaChannelBindingModel

  override def componentId: String = "/kafka-channel"

  override val key: StrField = fields.field(KafkaChannelBindingModel.key)

  override protected def bindingVersionField: Field = BindingVersion

  def withTopic(topic: String): this.type        = set(Topic, topic)
  def withPartitions(partitions: Int): this.type = set(Partitions, partitions)
  def withReplicas(replicas: Int): this.type     = set(Replicas, replicas)

  override def linkCopy(): KafkaChannelBinding = KafkaChannelBinding().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    KafkaChannelBinding.apply
}

object KafkaChannelBinding {
  def apply(): KafkaChannelBinding = apply(Annotations())

  def apply(annotations: Annotations): KafkaChannelBinding = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): KafkaChannelBinding =
    new KafkaChannelBinding(fields, annotations)
}
