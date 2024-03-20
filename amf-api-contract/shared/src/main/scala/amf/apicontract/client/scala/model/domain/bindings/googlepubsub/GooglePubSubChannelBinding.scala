package amf.apicontract.client.scala.model.domain.bindings.googlepubsub

import amf.apicontract.client.scala.model.domain.bindings.{BindingVersion, ChannelBinding}
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubChannelBindingModel._
import amf.apicontract.internal.metamodel.domain.bindings.GooglePubSubSchemaSettingsModel._
import amf.apicontract.internal.metamodel.domain.bindings.{GooglePubSubChannelBindingModel, GooglePubSubMessageStoragePolicyModel, GooglePubSubSchemaSettingsModel}
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.GooglePubSub
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.{Key, NodeShape}
class GooglePubSubChannelBinding(override val fields: Fields, override val annotations: Annotations)
    extends ChannelBinding
    with BindingVersion
    with Key {
  override protected def bindingVersionField: Field          = BindingVersion
  def labels: NodeShape                                      = fields.field(Labels)
  def messageRetentionDuration: StrField                     = fields.field(MessageRetentionDuration)
  def messageStoragePolicy: GooglePubSubMessageStoragePolicy = fields.field(MessageStoragePolicy)
  def schemaSettings: GooglePubSubSchemaSettings             = fields.field(SchemaSettings)
  def topic: StrField                                        = fields.field(Topic)

  def withLabels(labels: NodeShape): this.type = set(Labels, labels)
  def withMessageRetentionDuration(messageRetentionDuration: String): this.type =
    set(MessageRetentionDuration, messageRetentionDuration)
  def withSchemaSettings(schemaSettings: GooglePubSubSchemaSettings): this.type =
    set(SchemaSettings, schemaSettings)
  def withMessageStoragePolicy(messageStoragePolicy: GooglePubSubMessageStoragePolicy): this.type =
    set(MessageStoragePolicy, messageStoragePolicy)
  def withTopic(topic: String): this.type = set(Topic, topic)

  override def key: StrField = fields.field(GooglePubSubChannelBindingModel.key)

  override def linkCopy(): GooglePubSubChannelBinding = GooglePubSubChannelBinding().withId(id)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement =
    GooglePubSubChannelBinding.apply

  override def meta: GooglePubSubChannelBindingModel.type = GooglePubSubChannelBindingModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = s"/$GooglePubSub-operation"
}

object GooglePubSubChannelBinding {
  def apply(): GooglePubSubChannelBinding                         = apply(Annotations())
  def apply(annotations: Annotations): GooglePubSubChannelBinding = apply(Fields(), annotations)
  def apply(fields: Fields, annotations: Annotations): GooglePubSubChannelBinding =
    new GooglePubSubChannelBinding(fields, annotations)
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
