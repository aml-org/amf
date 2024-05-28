package amf.apicontract.client.scala.model.domain.bindings.googlepubsub

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubChannelBinding010Model.Topic
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubChannelBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubSchemaSettingsModel._
import amf.apicontract.internal.metamodel.domain.bindings.{
  GooglePubSubChannelBinding010Model,
  GooglePubSubChannelBinding020Model,
  GooglePubSubChannelBindingModel,
  GooglePubSubMessageStoragePolicyModel,
  GooglePubSubSchemaSettingsModel
}
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.GooglePubSub
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, ObjectNode}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.Key

abstract class GooglePubSubChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {
  override def componentId: String                  = s"/$GooglePubSub-channel"
  override def key: StrField                        = fields.field(GooglePubSubChannelBindingModel.key)
  override protected def bindingVersionField: Field = BindingVersion

  def labels: ObjectNode                                     = fields.field(Labels)
  def messageRetentionDuration: StrField                     = fields.field(MessageRetentionDuration)
  def messageStoragePolicy: GooglePubSubMessageStoragePolicy = fields.field(MessageStoragePolicy)
  def schemaSettings: GooglePubSubSchemaSettings             = fields.field(SchemaSettings)

  def withLabels(labels: ObjectNode): this.type = set(Labels, labels)
  def withMessageRetentionDuration(messageRetentionDuration: String): this.type =
    set(MessageRetentionDuration, messageRetentionDuration)
  def withSchemaSettings(schemaSettings: GooglePubSubSchemaSettings): this.type =
    set(SchemaSettings, schemaSettings)
  def withMessageStoragePolicy(messageStoragePolicy: GooglePubSubMessageStoragePolicy): this.type =
    set(MessageStoragePolicy, messageStoragePolicy)
}

class GooglePubSubChannelBinding010(override val fields: Fields, override val annotations: Annotations)
    extends GooglePubSubChannelBinding(fields, annotations) {
  override def linkCopy(): GooglePubSubChannelBinding010 = GooglePubSubChannelBinding010().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    GooglePubSubChannelBinding010.apply

  override def meta: GooglePubSubChannelBinding010Model.type = GooglePubSubChannelBinding010Model

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = s"/$GooglePubSub-channel-010"

  def topic: StrField                     = fields.field(Topic)
  def withTopic(topic: String): this.type = set(Topic, topic)
}

object GooglePubSubChannelBinding010 {
  def apply(): GooglePubSubChannelBinding010                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubChannelBinding010 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubChannelBinding010 =
    new GooglePubSubChannelBinding010(fields, annotations)
}

class GooglePubSubChannelBinding020(override val fields: Fields, override val annotations: Annotations)
    extends GooglePubSubChannelBinding(fields, annotations) {
  override def linkCopy(): GooglePubSubChannelBinding020 = GooglePubSubChannelBinding020().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    GooglePubSubChannelBinding020.apply

  override def meta: GooglePubSubChannelBinding020Model.type = GooglePubSubChannelBinding020Model

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = s"/$GooglePubSub-channel-020"
}

object GooglePubSubChannelBinding020 {
  def apply(): GooglePubSubChannelBinding020                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubChannelBinding020 = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubChannelBinding020 =
    new GooglePubSubChannelBinding020(fields, annotations)
}

class GooglePubSubMessageStoragePolicy(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement {
  def meta: GooglePubSubMessageStoragePolicyModel.type = GooglePubSubMessageStoragePolicyModel

  def allowedPersistenceRegions: Seq[StrField] =
    fields.field(GooglePubSubMessageStoragePolicyModel.AllowedPersistenceRegions)
  def withAllowedPersistenceRegions(allowedPersistenceRegions: Seq[String]): this.type =
    set(GooglePubSubMessageStoragePolicyModel.AllowedPersistenceRegions, allowedPersistenceRegions)

  override def componentId: String = s"/$GooglePubSub-messageStoragePolicy"
}

object GooglePubSubMessageStoragePolicy {
  def apply(): GooglePubSubMessageStoragePolicy                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubMessageStoragePolicy = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubMessageStoragePolicy =
    new GooglePubSubMessageStoragePolicy(fields, annotations)
}

class GooglePubSubSchemaSettings(val fields: Fields, val annotations: Annotations) extends DomainElement {
  def meta: GooglePubSubSchemaSettingsModel.type = GooglePubSubSchemaSettingsModel
  def encoding: StrField                         = fields.field(Encoding)
  def firstRevisionId: StrField                  = fields.field(FirstRevisionId)
  def lastRevisionId: StrField                   = fields.field(LastRevisionId)
  def name: StrField                             = fields.field(Name)

  def withEncoding(encoding: String): this.type               = set(Encoding, encoding)
  def withFirstRevisionId(firstRevisionId: String): this.type = set(FirstRevisionId, firstRevisionId)
  def withLastRevisionId(lastRevisionId: String): this.type   = set(LastRevisionId, lastRevisionId)
  def withName(name: String): this.type                       = set(Name, name)

  def componentId: String = s"/$GooglePubSub-schemaSettings"

}

object GooglePubSubSchemaSettings {
  def apply(): GooglePubSubSchemaSettings                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubSchemaSettings = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubSchemaSettings =
    new GooglePubSubSchemaSettings(fields, annotations)
}
