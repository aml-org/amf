package amf.apicontract.internal.convert

import amf.apicontract.client.platform
import amf.apicontract.client.platform.model.domain.federation
import amf.apicontract.client.platform.model.{document, domain}
import amf.apicontract.client.scala.model.document.{APIContractProcessingData, ComponentModule}
import amf.apicontract.client.scala.model.domain.bindings._
import amf.apicontract.client.scala.model.domain.bindings.amqp._
import amf.apicontract.client.scala.model.domain.bindings.anypointmq._
import amf.apicontract.client.scala.model.domain.bindings.googlepubsub._
import amf.apicontract.client.scala.model.domain.bindings.http._
import amf.apicontract.client.scala.model.domain.bindings.ibmmq._
import amf.apicontract.client.scala.model.domain.bindings.kafka._
import amf.apicontract.client.scala.model.domain.bindings.mqtt._
import amf.apicontract.client.scala.model.domain.bindings.websockets._
import amf.apicontract.client.scala.model.domain.federation._
import amf.apicontract.client.scala.model.domain.bindings.pulsar._
import amf.apicontract.client.scala.model.domain.bindings.solace._
import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.{AMFConfiguration, AMFDocumentResult, AMFLibraryResult}
import amf.core.internal.convert.{BidirectionalMatcher, CoreBaseConverter}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.convert.ShapesBaseConverter

trait ApiBaseConverter
    extends CoreBaseConverter
    with ShapesBaseConverter
    with EndPointConverter
    with ResourceTypeConverter
    with TraitConverter
    with OrganizationConverter
    with LicenseConverter
    with MessageConverter
    with OperationConverter
    with TagConverter
    with ParameterConverter
    with PayloadConverter
    with ParametrizedSecuritySchemeConverter
    with SecuritySchemeConverter
    with SettingsConverter
    with ScopeConverter
    with ServerConverter
    with TemplatedLinkConverter
    with CallbackConverter
    with EncodingConverter
    with OAuth2FlowConverter
    with SecurityRequirementConverter
    with CorrelationIdConverter
    with ChannelBindingsConverter
    with OperationBindingsConverter
    with ServerBindingsConverter
    with MessageBindingsConverter
    with Amqp091ChannelBindingConverter
    with Amqp091ChannelBinding010Converter
    with Amqp091ChannelBinding020Converter
    with Amqp091MessageBindingConverter
    with Amqp091OperationBindingConverter
    with Amqp091OperationBinding010Converter
    with Amqp091OperationBinding030Converter
    with EmptyBindingConverter
    with HttpMessageBindingConverter
    with HttpMessageBinding020Converter
    with HttpMessageBinding030Converter
    with HttpOperationBindingConverter
    with HttpOperationBinding010Converter
    with HttpOperationBinding020Converter
    with KafkaMessageBindingConverter
    with KafkaMessageBinding010Converter
    with KafkaMessageBinding030Converter
    with KafkaOperationBindingConverter
    with KafkaServerBindingConverter
    with KafkaChannelBindingConverter
    with KafkaChannelBinding030Converter
    with KafkaChannelBinding040Converter
    with KafkaChannelBinding050Converter
    with KafkaTopicConfigurationConverter
    with KafkaTopicConfiguration040Converter
    with KafkaTopicConfiguration050Converter
    with MqttMessageBindingConverter
    with MqttMessageBinding010Converter
    with MqttMessageBinding020Converter
    with MqttOperationBindingConverter
    with MqttOperationBinding010Converter
    with MqttOperationBinding020Converter
    with MqttServerBindingConverter
    with MqttServerBinding010Converter
    with MqttServerBinding020Converter
    with WebSocketsChannelBindingConverter
    with MqttServerLastWillConverter
    with Amqp091ChannelExchangeConverter
    with Amqp091ChannelExchange010Converter
    with Amqp091ChannelExchange020Converter
    with Amqp091QueueConverter
    with Amqp091Queue010Converter
    with Amqp091Queue020Converter
    with SolaceServerBindingConverter
    with SolaceServerBinding010Converter
    with SolaceServerBinding040Converter
    with SolaceOperationBindingConverter
    with SolaceOperationBinding010Converter
    with SolaceOperationBinding020Converter
    with SolaceOperationBinding030Converter
    with SolaceOperationBinding040Converter
    with SolaceOperationDestinationConverter
    with SolaceOperationDestination010Converter
    with SolaceOperationDestination020Converter
    with SolaceOperationDestination030Converter
    with SolaceOperationDestination040Converter
    with SolaceOperationQueueConverter
    with SolaceOperationQueue010Converter
    with SolaceOperationQueue030Converter
    with SolaceOperationTopicConverter
    with AnypointMQMessageBindingConverter
    with AnypointMQChannelBindingConverter
    with IBMMQMessageBindingConverter
    with IBMMQServerBindingConverter
    with IBMMQChannelBindingConverter
    with IBMMQChannelQueueConverter
    with IBMMQChannelTopicConverter
    with ChannelBindingConverter
    with OperationBindingConverter
    with MessageBindingConverter
    with ServerBindingConverter
    with AMFConfigurationConverter
    with AMFDocumentResultConverter
    with AMFLibraryResultConverter
    with APIContractProcessingDataConverter
    with OperationFederationMetadataConverter
    with ParameterKeyMappingConverter
    with ComponentModuleConverter
    with ParameterFederationMetadataConverter
    with EndpointFederationMetadataConverter
    with PulsarServerBindingConverter
    with PulsarChannelBindingConverter
    with PulsarChannelRetentionConverter
    with GooglePubSubMessageBindingConverter
    with GooglePubSubMessageBinding010Converter
    with GooglePubSubMessageBinding020Converter
    with GooglePubSubSchemaDefinitionConverter
    with GooglePubSubSchemaDefinition010Converter
    with GooglePubSubSchemaDefinition020Converter
    with GooglePubSubChannelBindingConverter
    with GooglePubSubChannelBinding010Converter
    with GooglePubSubChannelBinding020Converter
    with GooglePubSubMessageStoragePolicyConverter
    with GooglePubSubSchemaSettingsConverter

trait ChannelBindingConverter extends PlatformSecrets {
  implicit object ChannelBindingMatcher extends BidirectionalMatcher[ChannelBinding, domain.bindings.ChannelBinding] {
    override def asClient(from: ChannelBinding): domain.bindings.ChannelBinding =
      platform.wrap[domain.bindings.ChannelBinding](from)
    override def asInternal(from: domain.bindings.ChannelBinding): ChannelBinding = from._internal
  }
}
trait ChannelBindingsConverter extends PlatformSecrets {
  implicit object ChannelBindingsMatcher
      extends BidirectionalMatcher[ChannelBindings, domain.bindings.ChannelBindings] {
    override def asClient(from: ChannelBindings): domain.bindings.ChannelBindings =
      platform.wrap[domain.bindings.ChannelBindings](from)
    override def asInternal(from: domain.bindings.ChannelBindings): ChannelBindings = from._internal
  }
}
trait OperationBindingsConverter extends PlatformSecrets {
  implicit object OperationBindingsMatcher
      extends BidirectionalMatcher[OperationBindings, domain.bindings.OperationBindings] {
    override def asClient(from: OperationBindings): domain.bindings.OperationBindings =
      platform.wrap[domain.bindings.OperationBindings](from)
    override def asInternal(from: domain.bindings.OperationBindings): OperationBindings = from._internal
  }
}
trait MessageBindingsConverter extends PlatformSecrets {
  implicit object MessageBindingsMatcher
      extends BidirectionalMatcher[MessageBindings, domain.bindings.MessageBindings] {
    override def asClient(from: MessageBindings): domain.bindings.MessageBindings =
      platform.wrap[domain.bindings.MessageBindings](from)
    override def asInternal(from: domain.bindings.MessageBindings): MessageBindings = from._internal
  }
}
trait ServerBindingsConverter extends PlatformSecrets {
  implicit object ServerBindingsMatcher extends BidirectionalMatcher[ServerBindings, domain.bindings.ServerBindings] {
    override def asClient(from: ServerBindings): domain.bindings.ServerBindings =
      platform.wrap[domain.bindings.ServerBindings](from)
    override def asInternal(from: domain.bindings.ServerBindings): ServerBindings = from._internal
  }
}
trait OperationBindingConverter extends PlatformSecrets {
  implicit object OperationBindingMatcher
      extends BidirectionalMatcher[OperationBinding, domain.bindings.OperationBinding] {
    override def asClient(from: OperationBinding): domain.bindings.OperationBinding =
      platform.wrap[domain.bindings.OperationBinding](from)
    override def asInternal(from: domain.bindings.OperationBinding): OperationBinding = from._internal
  }
}
trait MessageBindingConverter extends PlatformSecrets {
  implicit object MessageBindingMatcher extends BidirectionalMatcher[MessageBinding, domain.bindings.MessageBinding] {
    override def asClient(from: MessageBinding): domain.bindings.MessageBinding =
      platform.wrap[domain.bindings.MessageBinding](from)
    override def asInternal(from: domain.bindings.MessageBinding): MessageBinding = from._internal
  }
}
trait ServerBindingConverter extends PlatformSecrets {
  implicit object ServerBindingMatcher extends BidirectionalMatcher[ServerBinding, domain.bindings.ServerBinding] {
    override def asClient(from: ServerBinding): domain.bindings.ServerBinding =
      platform.wrap[domain.bindings.ServerBinding](from)
    override def asInternal(from: domain.bindings.ServerBinding): ServerBinding = from._internal
  }
}
trait Amqp091ChannelBindingConverter extends PlatformSecrets {
  implicit object Amqp091ChannelBindingMatcher
      extends BidirectionalMatcher[Amqp091ChannelBinding, domain.bindings.amqp.Amqp091ChannelBinding] {
    override def asClient(from: Amqp091ChannelBinding): domain.bindings.amqp.Amqp091ChannelBinding =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelBinding](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelBinding): Amqp091ChannelBinding = from._internal
  }
}
trait Amqp091ChannelBinding010Converter extends PlatformSecrets {
  implicit object Amqp091ChannelBinding010Matcher
      extends BidirectionalMatcher[Amqp091ChannelBinding010, domain.bindings.amqp.Amqp091ChannelBinding010] {
    override def asClient(from: Amqp091ChannelBinding010): domain.bindings.amqp.Amqp091ChannelBinding010 =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelBinding010](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelBinding010): Amqp091ChannelBinding010 =
      from._internal
  }
}
trait Amqp091ChannelBinding020Converter extends PlatformSecrets {
  implicit object Amqp091ChannelBinding020Matcher
      extends BidirectionalMatcher[Amqp091ChannelBinding020, domain.bindings.amqp.Amqp091ChannelBinding020] {
    override def asClient(from: Amqp091ChannelBinding020): domain.bindings.amqp.Amqp091ChannelBinding020 =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelBinding020](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelBinding020): Amqp091ChannelBinding020 =
      from._internal
  }
}
trait Amqp091MessageBindingConverter extends PlatformSecrets {
  implicit object Amqp091MessageBindingMatcher
      extends BidirectionalMatcher[Amqp091MessageBinding, domain.bindings.amqp.Amqp091MessageBinding] {
    override def asClient(from: Amqp091MessageBinding): domain.bindings.amqp.Amqp091MessageBinding =
      platform.wrap[domain.bindings.amqp.Amqp091MessageBinding](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091MessageBinding): Amqp091MessageBinding = from._internal
  }
}
trait Amqp091OperationBindingConverter extends PlatformSecrets {
  implicit object Amqp091OperationBindingMatcher
      extends BidirectionalMatcher[Amqp091OperationBinding, domain.bindings.amqp.Amqp091OperationBinding] {
    override def asClient(from: Amqp091OperationBinding): domain.bindings.amqp.Amqp091OperationBinding =
      platform.wrap[domain.bindings.amqp.Amqp091OperationBinding](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091OperationBinding): Amqp091OperationBinding =
      from._internal
  }
}
trait Amqp091OperationBinding010Converter extends PlatformSecrets {
  implicit object Amqp091OperationBinding010Matcher
      extends BidirectionalMatcher[Amqp091OperationBinding010, domain.bindings.amqp.Amqp091OperationBinding010] {
    override def asClient(from: Amqp091OperationBinding010): domain.bindings.amqp.Amqp091OperationBinding010 =
      platform.wrap[domain.bindings.amqp.Amqp091OperationBinding010](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091OperationBinding010): Amqp091OperationBinding010 =
      from._internal
  }
}
trait Amqp091OperationBinding030Converter extends PlatformSecrets {
  implicit object Amqp091OperationBinding030Matcher
      extends BidirectionalMatcher[Amqp091OperationBinding030, domain.bindings.amqp.Amqp091OperationBinding030] {
    override def asClient(from: Amqp091OperationBinding030): domain.bindings.amqp.Amqp091OperationBinding030 =
      platform.wrap[domain.bindings.amqp.Amqp091OperationBinding030](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091OperationBinding030): Amqp091OperationBinding030 =
      from._internal
  }
}
trait EmptyBindingConverter extends PlatformSecrets {
  implicit object EmptyBindingMatcher extends BidirectionalMatcher[EmptyBinding, domain.bindings.EmptyBinding] {
    override def asClient(from: EmptyBinding): domain.bindings.EmptyBinding =
      platform.wrap[domain.bindings.EmptyBinding](from)
    override def asInternal(from: domain.bindings.EmptyBinding): EmptyBinding = from._internal
  }
}
trait HttpMessageBindingConverter extends PlatformSecrets {
  implicit object HttpMessageBindingMatcher
      extends BidirectionalMatcher[HttpMessageBinding, domain.bindings.http.HttpMessageBinding] {
    override def asClient(from: HttpMessageBinding): domain.bindings.http.HttpMessageBinding =
      platform.wrap[domain.bindings.http.HttpMessageBinding](from)
    override def asInternal(from: domain.bindings.http.HttpMessageBinding): HttpMessageBinding = from._internal
  }
}
trait HttpMessageBinding020Converter extends PlatformSecrets {
  implicit object HttpMessageBinding020Matcher
      extends BidirectionalMatcher[HttpMessageBinding020, domain.bindings.http.HttpMessageBinding020] {
    override def asClient(from: HttpMessageBinding020): domain.bindings.http.HttpMessageBinding020 =
      platform.wrap[domain.bindings.http.HttpMessageBinding020](from)
    override def asInternal(from: domain.bindings.http.HttpMessageBinding020): HttpMessageBinding020 = from._internal
  }
}
trait HttpMessageBinding030Converter extends PlatformSecrets {
  implicit object HttpMessageBinding030Matcher
      extends BidirectionalMatcher[HttpMessageBinding030, domain.bindings.http.HttpMessageBinding030] {
    override def asClient(from: HttpMessageBinding030): domain.bindings.http.HttpMessageBinding030 =
      platform.wrap[domain.bindings.http.HttpMessageBinding030](from)
    override def asInternal(from: domain.bindings.http.HttpMessageBinding030): HttpMessageBinding030 = from._internal
  }
}
trait HttpOperationBindingConverter extends PlatformSecrets {
  implicit object HttpOperationBindingMatcher
      extends BidirectionalMatcher[HttpOperationBinding, domain.bindings.http.HttpOperationBinding] {
    override def asClient(from: HttpOperationBinding): domain.bindings.http.HttpOperationBinding =
      platform.wrap[domain.bindings.http.HttpOperationBinding](from)
    override def asInternal(from: domain.bindings.http.HttpOperationBinding): HttpOperationBinding = from._internal
  }
}
trait HttpOperationBinding010Converter extends PlatformSecrets {
  implicit object HttpOperationBinding010Matcher
      extends BidirectionalMatcher[HttpOperationBinding010, domain.bindings.http.HttpOperationBinding010] {
    override def asClient(from: HttpOperationBinding010): domain.bindings.http.HttpOperationBinding010 =
      platform.wrap[domain.bindings.http.HttpOperationBinding010](from)
    override def asInternal(from: domain.bindings.http.HttpOperationBinding010): HttpOperationBinding010 =
      from._internal
  }
}
trait HttpOperationBinding020Converter extends PlatformSecrets {
  implicit object HttpOperationBinding020Matcher
      extends BidirectionalMatcher[HttpOperationBinding020, domain.bindings.http.HttpOperationBinding020] {
    override def asClient(from: HttpOperationBinding020): domain.bindings.http.HttpOperationBinding020 =
      platform.wrap[domain.bindings.http.HttpOperationBinding020](from)
    override def asInternal(from: domain.bindings.http.HttpOperationBinding020): HttpOperationBinding020 =
      from._internal
  }
}
trait KafkaMessageBindingConverter extends PlatformSecrets {
  implicit object KafkaMessageBindingMatcher
      extends BidirectionalMatcher[KafkaMessageBinding, domain.bindings.kafka.KafkaMessageBinding] {
    override def asClient(from: KafkaMessageBinding): domain.bindings.kafka.KafkaMessageBinding =
      platform.wrap[domain.bindings.kafka.KafkaMessageBinding](from)
    override def asInternal(from: domain.bindings.kafka.KafkaMessageBinding): KafkaMessageBinding = from._internal
  }
}
trait KafkaMessageBinding010Converter extends PlatformSecrets {
  implicit object KafkaMessageBinding010Matcher
      extends BidirectionalMatcher[KafkaMessageBinding010, domain.bindings.kafka.KafkaMessageBinding010] {
    override def asClient(from: KafkaMessageBinding010): domain.bindings.kafka.KafkaMessageBinding010 =
      platform.wrap[domain.bindings.kafka.KafkaMessageBinding010](from)
    override def asInternal(from: domain.bindings.kafka.KafkaMessageBinding010): KafkaMessageBinding010 = from._internal
  }
}
trait KafkaMessageBinding030Converter extends PlatformSecrets {
  implicit object KafkaMessageBinding030Matcher
      extends BidirectionalMatcher[KafkaMessageBinding030, domain.bindings.kafka.KafkaMessageBinding030] {
    override def asClient(from: KafkaMessageBinding030): domain.bindings.kafka.KafkaMessageBinding030 =
      platform.wrap[domain.bindings.kafka.KafkaMessageBinding030](from)
    override def asInternal(from: domain.bindings.kafka.KafkaMessageBinding030): KafkaMessageBinding030 = from._internal
  }
}
trait KafkaOperationBindingConverter extends PlatformSecrets {
  implicit object KafkaOperationBindingMatcher
      extends BidirectionalMatcher[KafkaOperationBinding, domain.bindings.kafka.KafkaOperationBinding] {
    override def asClient(from: KafkaOperationBinding): domain.bindings.kafka.KafkaOperationBinding =
      platform.wrap[domain.bindings.kafka.KafkaOperationBinding](from)
    override def asInternal(from: domain.bindings.kafka.KafkaOperationBinding): KafkaOperationBinding = from._internal
  }
}
trait KafkaServerBindingConverter extends PlatformSecrets {
  implicit object KafkaServerBindingMatcher
      extends BidirectionalMatcher[KafkaServerBinding, domain.bindings.kafka.KafkaServerBinding] {
    override def asClient(from: KafkaServerBinding): domain.bindings.kafka.KafkaServerBinding =
      platform.wrap[domain.bindings.kafka.KafkaServerBinding](from)
    override def asInternal(from: domain.bindings.kafka.KafkaServerBinding): KafkaServerBinding = from._internal
  }
}
trait KafkaChannelBindingConverter extends PlatformSecrets {
  implicit object KafkaChannelBindingMatcher
      extends BidirectionalMatcher[KafkaChannelBinding, domain.bindings.kafka.KafkaChannelBinding] {
    override def asClient(from: KafkaChannelBinding): domain.bindings.kafka.KafkaChannelBinding =
      platform.wrap[domain.bindings.kafka.KafkaChannelBinding](from)
    override def asInternal(from: domain.bindings.kafka.KafkaChannelBinding): KafkaChannelBinding = from._internal
  }
}
trait KafkaChannelBinding030Converter extends PlatformSecrets {
  implicit object KafkaChannelBinding030Matcher
      extends BidirectionalMatcher[KafkaChannelBinding030, domain.bindings.kafka.KafkaChannelBinding030] {
    override def asClient(from: KafkaChannelBinding030): domain.bindings.kafka.KafkaChannelBinding030 =
      platform.wrap[domain.bindings.kafka.KafkaChannelBinding030](from)
    override def asInternal(from: domain.bindings.kafka.KafkaChannelBinding030): KafkaChannelBinding030 = from._internal
  }
}
trait KafkaChannelBinding040Converter extends PlatformSecrets {
  implicit object KafkaChannelBinding040Matcher
      extends BidirectionalMatcher[KafkaChannelBinding040, domain.bindings.kafka.KafkaChannelBinding040] {
    override def asClient(from: KafkaChannelBinding040): domain.bindings.kafka.KafkaChannelBinding040 =
      platform.wrap[domain.bindings.kafka.KafkaChannelBinding040](from)
    override def asInternal(from: domain.bindings.kafka.KafkaChannelBinding040): KafkaChannelBinding040 = from._internal
  }
}
trait KafkaChannelBinding050Converter extends PlatformSecrets {
  implicit object KafkaChannelBinding050Matcher
      extends BidirectionalMatcher[KafkaChannelBinding050, domain.bindings.kafka.KafkaChannelBinding050] {
    override def asClient(from: KafkaChannelBinding050): domain.bindings.kafka.KafkaChannelBinding050 =
      platform.wrap[domain.bindings.kafka.KafkaChannelBinding050](from)
    override def asInternal(from: domain.bindings.kafka.KafkaChannelBinding050): KafkaChannelBinding050 = from._internal
  }
}
trait KafkaTopicConfigurationConverter extends PlatformSecrets {
  implicit object KafkaTopicConfigurationMatcher
      extends BidirectionalMatcher[KafkaTopicConfiguration, domain.bindings.kafka.KafkaTopicConfiguration] {
    override def asClient(from: KafkaTopicConfiguration): domain.bindings.kafka.KafkaTopicConfiguration =
      platform.wrap[domain.bindings.kafka.KafkaTopicConfiguration](from)
    override def asInternal(from: domain.bindings.kafka.KafkaTopicConfiguration): KafkaTopicConfiguration =
      from._internal
  }
}
trait KafkaTopicConfiguration040Converter extends PlatformSecrets {
  implicit object KafkaTopicConfiguration040Matcher
      extends BidirectionalMatcher[KafkaTopicConfiguration040, domain.bindings.kafka.KafkaTopicConfiguration040] {
    override def asClient(from: KafkaTopicConfiguration040): domain.bindings.kafka.KafkaTopicConfiguration040 =
      platform.wrap[domain.bindings.kafka.KafkaTopicConfiguration040](from)
    override def asInternal(from: domain.bindings.kafka.KafkaTopicConfiguration040): KafkaTopicConfiguration040 =
      from._internal
  }
}
trait KafkaTopicConfiguration050Converter extends PlatformSecrets {
  implicit object KafkaTopicConfiguration050Matcher
      extends BidirectionalMatcher[KafkaTopicConfiguration050, domain.bindings.kafka.KafkaTopicConfiguration050] {
    override def asClient(from: KafkaTopicConfiguration050): domain.bindings.kafka.KafkaTopicConfiguration050 =
      platform.wrap[domain.bindings.kafka.KafkaTopicConfiguration050](from)
    override def asInternal(from: domain.bindings.kafka.KafkaTopicConfiguration050): KafkaTopicConfiguration050 =
      from._internal
  }
}
trait MqttMessageBindingConverter extends PlatformSecrets {
  implicit object MqttMessageBindingMatcher
      extends BidirectionalMatcher[MqttMessageBinding, domain.bindings.mqtt.MqttMessageBinding] {
    override def asClient(from: MqttMessageBinding): domain.bindings.mqtt.MqttMessageBinding =
      platform.wrap[domain.bindings.mqtt.MqttMessageBinding](from)
    override def asInternal(from: domain.bindings.mqtt.MqttMessageBinding): MqttMessageBinding = from._internal
  }
}
trait MqttMessageBinding010Converter extends PlatformSecrets {
  implicit object MqttMessageBinding010Matcher
      extends BidirectionalMatcher[MqttMessageBinding010, domain.bindings.mqtt.MqttMessageBinding010] {
    override def asClient(from: MqttMessageBinding010): domain.bindings.mqtt.MqttMessageBinding010 =
      platform.wrap[domain.bindings.mqtt.MqttMessageBinding010](from)
    override def asInternal(from: domain.bindings.mqtt.MqttMessageBinding010): MqttMessageBinding010 = from._internal
  }
}
trait MqttMessageBinding020Converter extends PlatformSecrets {
  implicit object MqttMessageBinding020Matcher
      extends BidirectionalMatcher[MqttMessageBinding020, domain.bindings.mqtt.MqttMessageBinding020] {
    override def asClient(from: MqttMessageBinding020): domain.bindings.mqtt.MqttMessageBinding020 =
      platform.wrap[domain.bindings.mqtt.MqttMessageBinding020](from)
    override def asInternal(from: domain.bindings.mqtt.MqttMessageBinding020): MqttMessageBinding020 = from._internal
  }
}
trait MqttOperationBindingConverter extends PlatformSecrets {
  implicit object MqttOperationBindingMatcher
      extends BidirectionalMatcher[MqttOperationBinding, domain.bindings.mqtt.MqttOperationBinding] {
    override def asClient(from: MqttOperationBinding): domain.bindings.mqtt.MqttOperationBinding =
      platform.wrap[domain.bindings.mqtt.MqttOperationBinding](from)
    override def asInternal(from: domain.bindings.mqtt.MqttOperationBinding): MqttOperationBinding = from._internal
  }
}
trait MqttOperationBinding010Converter extends PlatformSecrets {
  implicit object MqttOperationBinding010Matcher
      extends BidirectionalMatcher[MqttOperationBinding010, domain.bindings.mqtt.MqttOperationBinding010] {
    override def asClient(from: MqttOperationBinding010): domain.bindings.mqtt.MqttOperationBinding010 =
      platform.wrap[domain.bindings.mqtt.MqttOperationBinding010](from)
    override def asInternal(from: domain.bindings.mqtt.MqttOperationBinding010): MqttOperationBinding010 =
      from._internal
  }
}
trait MqttOperationBinding020Converter extends PlatformSecrets {
  implicit object MqttOperationBinding020Matcher
      extends BidirectionalMatcher[MqttOperationBinding020, domain.bindings.mqtt.MqttOperationBinding020] {
    override def asClient(from: MqttOperationBinding020): domain.bindings.mqtt.MqttOperationBinding020 =
      platform.wrap[domain.bindings.mqtt.MqttOperationBinding020](from)
    override def asInternal(from: domain.bindings.mqtt.MqttOperationBinding020): MqttOperationBinding020 =
      from._internal
  }
}
trait MqttServerBindingConverter extends PlatformSecrets {
  implicit object MqttServerBindingMatcher
      extends BidirectionalMatcher[MqttServerBinding, domain.bindings.mqtt.MqttServerBinding] {
    override def asClient(from: MqttServerBinding): domain.bindings.mqtt.MqttServerBinding =
      platform.wrap[domain.bindings.mqtt.MqttServerBinding](from)
    override def asInternal(from: domain.bindings.mqtt.MqttServerBinding): MqttServerBinding = from._internal
  }
}
trait MqttServerBinding010Converter extends PlatformSecrets {
  implicit object MqttServerBinding010Matcher
      extends BidirectionalMatcher[MqttServerBinding010, domain.bindings.mqtt.MqttServerBinding010] {
    override def asClient(from: MqttServerBinding010): domain.bindings.mqtt.MqttServerBinding010 =
      platform.wrap[domain.bindings.mqtt.MqttServerBinding010](from)
    override def asInternal(from: domain.bindings.mqtt.MqttServerBinding010): MqttServerBinding010 = from._internal
  }
}
trait MqttServerBinding020Converter extends PlatformSecrets {
  implicit object MqttServerBinding020Matcher
      extends BidirectionalMatcher[MqttServerBinding020, domain.bindings.mqtt.MqttServerBinding020] {
    override def asClient(from: MqttServerBinding020): domain.bindings.mqtt.MqttServerBinding020 =
      platform.wrap[domain.bindings.mqtt.MqttServerBinding020](from)
    override def asInternal(from: domain.bindings.mqtt.MqttServerBinding020): MqttServerBinding020 = from._internal
  }
}
trait WebSocketsChannelBindingConverter extends PlatformSecrets {
  implicit object WebSocketsChannelBindingMatcher
      extends BidirectionalMatcher[WebSocketsChannelBinding, domain.bindings.websockets.WebSocketsChannelBinding] {
    override def asClient(from: WebSocketsChannelBinding): domain.bindings.websockets.WebSocketsChannelBinding =
      platform.wrap[domain.bindings.websockets.WebSocketsChannelBinding](from)
    override def asInternal(from: domain.bindings.websockets.WebSocketsChannelBinding): WebSocketsChannelBinding =
      from._internal
  }
}

trait MqttServerLastWillConverter extends PlatformSecrets {
  implicit object MqttServerLastWillMatcher
      extends BidirectionalMatcher[MqttServerLastWill, domain.bindings.mqtt.MqttServerLastWill] {
    override def asClient(from: MqttServerLastWill): domain.bindings.mqtt.MqttServerLastWill =
      platform.wrap[domain.bindings.mqtt.MqttServerLastWill](from)
    override def asInternal(from: domain.bindings.mqtt.MqttServerLastWill): MqttServerLastWill = from._internal
  }
}

trait Amqp091ChannelExchangeConverter extends PlatformSecrets {
  implicit object Amqp091ChannelExchangeMatcher
      extends BidirectionalMatcher[Amqp091ChannelExchange, domain.bindings.amqp.Amqp091ChannelExchange] {
    override def asClient(from: Amqp091ChannelExchange): domain.bindings.amqp.Amqp091ChannelExchange =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelExchange](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelExchange): Amqp091ChannelExchange = from._internal
  }
}

trait Amqp091ChannelExchange010Converter extends PlatformSecrets {
  implicit object Amqp091ChannelExchange010Matcher
      extends BidirectionalMatcher[Amqp091ChannelExchange010, domain.bindings.amqp.Amqp091ChannelExchange010] {
    override def asClient(from: Amqp091ChannelExchange010): domain.bindings.amqp.Amqp091ChannelExchange010 =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelExchange010](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelExchange010): Amqp091ChannelExchange010 =
      from._internal
  }
}

trait Amqp091ChannelExchange020Converter extends PlatformSecrets {
  implicit object Amqp091ChannelExchange020Matcher
      extends BidirectionalMatcher[Amqp091ChannelExchange020, domain.bindings.amqp.Amqp091ChannelExchange020] {
    override def asClient(from: Amqp091ChannelExchange020): domain.bindings.amqp.Amqp091ChannelExchange020 =
      platform.wrap[domain.bindings.amqp.Amqp091ChannelExchange020](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091ChannelExchange020): Amqp091ChannelExchange020 =
      from._internal
  }
}

trait Amqp091QueueConverter extends PlatformSecrets {
  implicit object Amqp091QueueMatcher extends BidirectionalMatcher[Amqp091Queue, domain.bindings.amqp.Amqp091Queue] {
    override def asClient(from: Amqp091Queue): domain.bindings.amqp.Amqp091Queue =
      platform.wrap[domain.bindings.amqp.Amqp091Queue](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091Queue): Amqp091Queue = from._internal
  }
}

trait Amqp091Queue010Converter extends PlatformSecrets {
  implicit object Amqp091Queue010Matcher
      extends BidirectionalMatcher[Amqp091Queue010, domain.bindings.amqp.Amqp091Queue010] {
    override def asClient(from: Amqp091Queue010): domain.bindings.amqp.Amqp091Queue010 =
      platform.wrap[domain.bindings.amqp.Amqp091Queue010](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091Queue010): Amqp091Queue010 = from._internal
  }
}

trait Amqp091Queue020Converter extends PlatformSecrets {
  implicit object Amqp091Queue020Matcher
      extends BidirectionalMatcher[Amqp091Queue020, domain.bindings.amqp.Amqp091Queue020] {
    override def asClient(from: Amqp091Queue020): domain.bindings.amqp.Amqp091Queue020 =
      platform.wrap[domain.bindings.amqp.Amqp091Queue020](from)
    override def asInternal(from: domain.bindings.amqp.Amqp091Queue020): Amqp091Queue020 = from._internal
  }
}

trait PulsarServerBindingConverter extends PlatformSecrets {
  implicit object PulsarServerBindingMatcher
      extends BidirectionalMatcher[PulsarServerBinding, domain.bindings.pulsar.PulsarServerBinding] {
    override def asClient(from: PulsarServerBinding): domain.bindings.pulsar.PulsarServerBinding =
      platform.wrap[domain.bindings.pulsar.PulsarServerBinding](from)
    override def asInternal(from: domain.bindings.pulsar.PulsarServerBinding): PulsarServerBinding =
      from._internal
  }
}

trait PulsarChannelBindingConverter extends PlatformSecrets {
  implicit object PulsarChannelBindingMatcher
      extends BidirectionalMatcher[PulsarChannelBinding, domain.bindings.pulsar.PulsarChannelBinding] {
    override def asClient(from: PulsarChannelBinding): domain.bindings.pulsar.PulsarChannelBinding =
      platform.wrap[domain.bindings.pulsar.PulsarChannelBinding](from)
    override def asInternal(from: domain.bindings.pulsar.PulsarChannelBinding): PulsarChannelBinding =
      from._internal
  }
}

trait PulsarChannelRetentionConverter extends PlatformSecrets {
  implicit object PulsarChannelRetentionMatcher
      extends BidirectionalMatcher[PulsarChannelRetention, domain.bindings.pulsar.PulsarChannelRetention] {
    override def asClient(from: PulsarChannelRetention): domain.bindings.pulsar.PulsarChannelRetention =
      platform.wrap[domain.bindings.pulsar.PulsarChannelRetention](from)
    override def asInternal(from: domain.bindings.pulsar.PulsarChannelRetention): PulsarChannelRetention =
      from._internal
  }
}

trait SolaceServerBindingConverter extends PlatformSecrets {
  implicit object SolaceServerBindingMatcher
      extends BidirectionalMatcher[SolaceServerBinding, domain.bindings.solace.SolaceServerBinding] {
    override def asClient(from: SolaceServerBinding): domain.bindings.solace.SolaceServerBinding =
      platform.wrap[domain.bindings.solace.SolaceServerBinding](from)
    override def asInternal(from: domain.bindings.solace.SolaceServerBinding): SolaceServerBinding =
      from._internal
  }
}
trait SolaceServerBinding010Converter extends PlatformSecrets {
  implicit object SolaceServerBinding010Matcher
      extends BidirectionalMatcher[SolaceServerBinding010, domain.bindings.solace.SolaceServerBinding010] {
    override def asClient(from: SolaceServerBinding010): domain.bindings.solace.SolaceServerBinding010 =
      platform.wrap[domain.bindings.solace.SolaceServerBinding010](from)
    override def asInternal(from: domain.bindings.solace.SolaceServerBinding010): SolaceServerBinding010 =
      from._internal
  }
}
trait SolaceServerBinding040Converter extends PlatformSecrets {
  implicit object SolaceServerBinding040Matcher
      extends BidirectionalMatcher[SolaceServerBinding040, domain.bindings.solace.SolaceServerBinding040] {
    override def asClient(from: SolaceServerBinding040): domain.bindings.solace.SolaceServerBinding040 =
      platform.wrap[domain.bindings.solace.SolaceServerBinding040](from)
    override def asInternal(from: domain.bindings.solace.SolaceServerBinding040): SolaceServerBinding040 =
      from._internal
  }
}

trait SolaceOperationBindingConverter extends PlatformSecrets {
  implicit object SolaceOperationBindingMatcher
      extends BidirectionalMatcher[SolaceOperationBinding, domain.bindings.solace.SolaceOperationBinding] {
    override def asClient(from: SolaceOperationBinding): domain.bindings.solace.SolaceOperationBinding =
      platform.wrap[domain.bindings.solace.SolaceOperationBinding](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationBinding): SolaceOperationBinding =
      from._internal
  }
}

trait SolaceOperationBinding010Converter extends PlatformSecrets {
  implicit object SolaceOperationBinding010Matcher
      extends BidirectionalMatcher[
        SolaceOperationBinding010,
        domain.bindings.solace.SolaceOperationBinding010
      ] {
    override def asClient(from: SolaceOperationBinding010): domain.bindings.solace.SolaceOperationBinding010 =
      platform.wrap[domain.bindings.solace.SolaceOperationBinding010](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationBinding010): SolaceOperationBinding010 =
      from._internal
  }
}

trait SolaceOperationBinding020Converter extends PlatformSecrets {
  implicit object SolaceOperationBinding020Matcher
      extends BidirectionalMatcher[
        SolaceOperationBinding020,
        domain.bindings.solace.SolaceOperationBinding020
      ] {
    override def asClient(from: SolaceOperationBinding020): domain.bindings.solace.SolaceOperationBinding020 =
      platform.wrap[domain.bindings.solace.SolaceOperationBinding020](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationBinding020): SolaceOperationBinding020 =
      from._internal
  }
}
trait SolaceOperationBinding030Converter extends PlatformSecrets {
  implicit object SolaceOperationBinding030Matcher
      extends BidirectionalMatcher[
        SolaceOperationBinding030,
        domain.bindings.solace.SolaceOperationBinding030
      ] {
    override def asClient(from: SolaceOperationBinding030): domain.bindings.solace.SolaceOperationBinding030 =
      platform.wrap[domain.bindings.solace.SolaceOperationBinding030](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationBinding030): SolaceOperationBinding030 =
      from._internal
  }
}
trait SolaceOperationBinding040Converter extends PlatformSecrets {
  implicit object SolaceOperationBinding040Matcher
      extends BidirectionalMatcher[
        SolaceOperationBinding040,
        domain.bindings.solace.SolaceOperationBinding040
      ] {
    override def asClient(from: SolaceOperationBinding040): domain.bindings.solace.SolaceOperationBinding040 =
      platform.wrap[domain.bindings.solace.SolaceOperationBinding040](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationBinding040): SolaceOperationBinding040 =
      from._internal
  }
}

trait SolaceOperationDestinationConverter extends PlatformSecrets {
  implicit object SolaceOperationDestinationMatcher
      extends BidirectionalMatcher[SolaceOperationDestination, domain.bindings.solace.SolaceOperationDestination] {
    override def asClient(from: SolaceOperationDestination): domain.bindings.solace.SolaceOperationDestination =
      platform.wrap[domain.bindings.solace.SolaceOperationDestination](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationDestination): SolaceOperationDestination =
      from._internal
  }
}
trait SolaceOperationDestination010Converter extends PlatformSecrets {
  implicit object SolaceOperationDestination010Matcher
      extends BidirectionalMatcher[
        SolaceOperationDestination010,
        domain.bindings.solace.SolaceOperationDestination010
      ] {
    override def asClient(from: SolaceOperationDestination010): domain.bindings.solace.SolaceOperationDestination010 =
      platform.wrap[domain.bindings.solace.SolaceOperationDestination010](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationDestination010): SolaceOperationDestination010 =
      from._internal
  }
}

trait SolaceOperationDestination020Converter extends PlatformSecrets {
  implicit object SolaceOperationDestination020Matcher
      extends BidirectionalMatcher[
        SolaceOperationDestination020,
        domain.bindings.solace.SolaceOperationDestination020
      ] {
    override def asClient(from: SolaceOperationDestination020): domain.bindings.solace.SolaceOperationDestination020 =
      platform.wrap[domain.bindings.solace.SolaceOperationDestination020](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationDestination020): SolaceOperationDestination020 =
      from._internal
  }
}
trait SolaceOperationDestination030Converter extends PlatformSecrets {
  implicit object SolaceOperationDestination030Matcher
      extends BidirectionalMatcher[
        SolaceOperationDestination030,
        domain.bindings.solace.SolaceOperationDestination030
      ] {
    override def asClient(from: SolaceOperationDestination030): domain.bindings.solace.SolaceOperationDestination030 =
      platform.wrap[domain.bindings.solace.SolaceOperationDestination030](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationDestination030): SolaceOperationDestination030 =
      from._internal
  }
}
trait SolaceOperationDestination040Converter extends PlatformSecrets {
  implicit object SolaceOperationDestination040Matcher
      extends BidirectionalMatcher[
        SolaceOperationDestination040,
        domain.bindings.solace.SolaceOperationDestination040
      ] {
    override def asClient(from: SolaceOperationDestination040): domain.bindings.solace.SolaceOperationDestination040 =
      platform.wrap[domain.bindings.solace.SolaceOperationDestination040](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationDestination040): SolaceOperationDestination040 =
      from._internal
  }
}

trait SolaceOperationQueueConverter extends PlatformSecrets {
  implicit object SolaceOperationQueueMatcher
      extends BidirectionalMatcher[SolaceOperationQueue, domain.bindings.solace.SolaceOperationQueue] {
    override def asClient(from: SolaceOperationQueue): domain.bindings.solace.SolaceOperationQueue =
      platform.wrap[domain.bindings.solace.SolaceOperationQueue](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationQueue): SolaceOperationQueue =
      from._internal
  }
}

trait SolaceOperationQueue010Converter extends PlatformSecrets {
  implicit object SolaceOperationQueue010Matcher
      extends BidirectionalMatcher[SolaceOperationQueue010, domain.bindings.solace.SolaceOperationQueue010] {
    override def asClient(from: SolaceOperationQueue010): domain.bindings.solace.SolaceOperationQueue010 =
      platform.wrap[domain.bindings.solace.SolaceOperationQueue010](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationQueue010): SolaceOperationQueue010 =
      from._internal
  }
}

trait SolaceOperationQueue030Converter extends PlatformSecrets {
  implicit object SolaceOperationQueue030Matcher
      extends BidirectionalMatcher[SolaceOperationQueue030, domain.bindings.solace.SolaceOperationQueue030] {
    override def asClient(from: SolaceOperationQueue030): domain.bindings.solace.SolaceOperationQueue030 =
      platform.wrap[domain.bindings.solace.SolaceOperationQueue030](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationQueue030): SolaceOperationQueue030 =
      from._internal
  }
}

trait SolaceOperationTopicConverter extends PlatformSecrets {
  implicit object SolaceOperationTopicMatcher
      extends BidirectionalMatcher[SolaceOperationTopic, domain.bindings.solace.SolaceOperationTopic] {
    override def asClient(from: SolaceOperationTopic): domain.bindings.solace.SolaceOperationTopic =
      platform.wrap[domain.bindings.solace.SolaceOperationTopic](from)
    override def asInternal(from: domain.bindings.solace.SolaceOperationTopic): SolaceOperationTopic =
      from._internal
  }
}

trait AnypointMQMessageBindingConverter extends PlatformSecrets {
  implicit object AnypointMQMessageBindingMatcher
      extends BidirectionalMatcher[AnypointMQMessageBinding, domain.bindings.anypointmq.AnypointMQMessageBinding] {
    override def asClient(from: AnypointMQMessageBinding): domain.bindings.anypointmq.AnypointMQMessageBinding =
      platform.wrap[domain.bindings.anypointmq.AnypointMQMessageBinding](from)
    override def asInternal(from: domain.bindings.anypointmq.AnypointMQMessageBinding): AnypointMQMessageBinding =
      from._internal
  }
}

trait AnypointMQChannelBindingConverter extends PlatformSecrets {
  implicit object AnypointMQChannelBindingMatcher
      extends BidirectionalMatcher[AnypointMQChannelBinding, domain.bindings.anypointmq.AnypointMQChannelBinding] {
    override def asClient(from: AnypointMQChannelBinding): domain.bindings.anypointmq.AnypointMQChannelBinding =
      platform.wrap[domain.bindings.anypointmq.AnypointMQChannelBinding](from)
    override def asInternal(from: domain.bindings.anypointmq.AnypointMQChannelBinding): AnypointMQChannelBinding =
      from._internal
  }
}

trait IBMMQMessageBindingConverter extends PlatformSecrets {
  implicit object IBMMQMessageBindingMatcher
      extends BidirectionalMatcher[IBMMQMessageBinding, domain.bindings.ibmmq.IBMMQMessageBinding] {
    override def asClient(from: IBMMQMessageBinding): domain.bindings.ibmmq.IBMMQMessageBinding =
      platform.wrap[domain.bindings.ibmmq.IBMMQMessageBinding](from)
    override def asInternal(from: domain.bindings.ibmmq.IBMMQMessageBinding): IBMMQMessageBinding = from._internal
  }
}

trait IBMMQServerBindingConverter extends PlatformSecrets {
  implicit object IBMMQServerBindingMatcher
      extends BidirectionalMatcher[IBMMQServerBinding, domain.bindings.ibmmq.IBMMQServerBinding] {
    override def asClient(from: IBMMQServerBinding): domain.bindings.ibmmq.IBMMQServerBinding =
      platform.wrap[domain.bindings.ibmmq.IBMMQServerBinding](from)
    override def asInternal(from: domain.bindings.ibmmq.IBMMQServerBinding): IBMMQServerBinding = from._internal
  }
}

trait IBMMQChannelBindingConverter extends PlatformSecrets {
  implicit object IBMMQChannelBindingMatcher
      extends BidirectionalMatcher[IBMMQChannelBinding, domain.bindings.ibmmq.IBMMQChannelBinding] {
    override def asClient(from: IBMMQChannelBinding): domain.bindings.ibmmq.IBMMQChannelBinding =
      platform.wrap[domain.bindings.ibmmq.IBMMQChannelBinding](from)
    override def asInternal(from: domain.bindings.ibmmq.IBMMQChannelBinding): IBMMQChannelBinding = from._internal
  }
}

trait IBMMQChannelQueueConverter extends PlatformSecrets {
  implicit object IBMMQChannelQueueMatcher
      extends BidirectionalMatcher[IBMMQChannelQueue, domain.bindings.ibmmq.IBMMQChannelQueue] {
    override def asClient(from: IBMMQChannelQueue): domain.bindings.ibmmq.IBMMQChannelQueue =
      platform.wrap[domain.bindings.ibmmq.IBMMQChannelQueue](from)
    override def asInternal(from: domain.bindings.ibmmq.IBMMQChannelQueue): IBMMQChannelQueue = from._internal
  }
}

trait IBMMQChannelTopicConverter extends PlatformSecrets {
  implicit object IBMMQChannelTopicMatcher
      extends BidirectionalMatcher[IBMMQChannelTopic, domain.bindings.ibmmq.IBMMQChannelTopic] {
    override def asClient(from: IBMMQChannelTopic): domain.bindings.ibmmq.IBMMQChannelTopic =
      platform.wrap[domain.bindings.ibmmq.IBMMQChannelTopic](from)
    override def asInternal(from: domain.bindings.ibmmq.IBMMQChannelTopic): IBMMQChannelTopic = from._internal
  }
}

trait GooglePubSubMessageBindingConverter extends PlatformSecrets {
  implicit object GooglePubSubMessageBindingMatcher
      extends BidirectionalMatcher[
        GooglePubSubMessageBinding,
        domain.bindings.googlepubsub.GooglePubSubMessageBinding
      ] {
    override def asClient(from: GooglePubSubMessageBinding): domain.bindings.googlepubsub.GooglePubSubMessageBinding =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubMessageBinding](from)
    override def asInternal(from: domain.bindings.googlepubsub.GooglePubSubMessageBinding): GooglePubSubMessageBinding =
      from._internal
  }
}
trait GooglePubSubMessageBinding010Converter extends PlatformSecrets {
  implicit object GooglePubSubMessageBinding010Matcher
      extends BidirectionalMatcher[
        GooglePubSubMessageBinding010,
        domain.bindings.googlepubsub.GooglePubSubMessageBinding010
      ] {
    override def asClient(
        from: GooglePubSubMessageBinding010
    ): domain.bindings.googlepubsub.GooglePubSubMessageBinding010 =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubMessageBinding010](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubMessageBinding010
    ): GooglePubSubMessageBinding010 =
      from._internal
  }
}
trait GooglePubSubMessageBinding020Converter extends PlatformSecrets {
  implicit object GooglePubSubMessageBinding020Matcher
      extends BidirectionalMatcher[
        GooglePubSubMessageBinding020,
        domain.bindings.googlepubsub.GooglePubSubMessageBinding020
      ] {
    override def asClient(
        from: GooglePubSubMessageBinding020
    ): domain.bindings.googlepubsub.GooglePubSubMessageBinding020 =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubMessageBinding020](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubMessageBinding020
    ): GooglePubSubMessageBinding020 =
      from._internal
  }
}
trait GooglePubSubSchemaDefinitionConverter extends PlatformSecrets {
  implicit object GooglePubSubSchemaDefinitionMatcher
      extends BidirectionalMatcher[
        GooglePubSubSchemaDefinition,
        domain.bindings.googlepubsub.GooglePubSubSchemaDefinition
      ] {
    override def asClient(
        from: GooglePubSubSchemaDefinition
    ): domain.bindings.googlepubsub.GooglePubSubSchemaDefinition =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubSchemaDefinition](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubSchemaDefinition
    ): GooglePubSubSchemaDefinition = from._internal
  }
}
trait GooglePubSubSchemaDefinition010Converter extends PlatformSecrets {
  implicit object GooglePubSubSchemaDefinition010Matcher
      extends BidirectionalMatcher[
        GooglePubSubSchemaDefinition010,
        domain.bindings.googlepubsub.GooglePubSubSchemaDefinition010
      ] {
    override def asClient(
        from: GooglePubSubSchemaDefinition010
    ): domain.bindings.googlepubsub.GooglePubSubSchemaDefinition010 =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubSchemaDefinition010](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubSchemaDefinition010
    ): GooglePubSubSchemaDefinition010 = from._internal
  }
}
trait GooglePubSubSchemaDefinition020Converter extends PlatformSecrets {
  implicit object GooglePubSubSchemaDefinition020Matcher
      extends BidirectionalMatcher[
        GooglePubSubSchemaDefinition020,
        domain.bindings.googlepubsub.GooglePubSubSchemaDefinition020
      ] {
    override def asClient(
        from: GooglePubSubSchemaDefinition020
    ): domain.bindings.googlepubsub.GooglePubSubSchemaDefinition020 =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubSchemaDefinition020](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubSchemaDefinition020
    ): GooglePubSubSchemaDefinition020 = from._internal
  }
}
trait GooglePubSubChannelBindingConverter extends PlatformSecrets {
  implicit object GooglePubSubChannelBindingMatcher
      extends BidirectionalMatcher[
        GooglePubSubChannelBinding,
        domain.bindings.googlepubsub.GooglePubSubChannelBinding
      ] {
    override def asClient(from: GooglePubSubChannelBinding): domain.bindings.googlepubsub.GooglePubSubChannelBinding =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubChannelBinding](from)
    override def asInternal(from: domain.bindings.googlepubsub.GooglePubSubChannelBinding): GooglePubSubChannelBinding =
      from._internal
  }
}
trait GooglePubSubChannelBinding010Converter extends PlatformSecrets {
  implicit object GooglePubSubChannelBinding010Matcher
      extends BidirectionalMatcher[
        GooglePubSubChannelBinding010,
        domain.bindings.googlepubsub.GooglePubSubChannelBinding010
      ] {
    override def asClient(
        from: GooglePubSubChannelBinding010
    ): domain.bindings.googlepubsub.GooglePubSubChannelBinding010 =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubChannelBinding010](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubChannelBinding010
    ): GooglePubSubChannelBinding010 =
      from._internal
  }
}
trait GooglePubSubChannelBinding020Converter extends PlatformSecrets {
  implicit object GooglePubSubChannelBinding020Matcher
      extends BidirectionalMatcher[
        GooglePubSubChannelBinding020,
        domain.bindings.googlepubsub.GooglePubSubChannelBinding020
      ] {
    override def asClient(
        from: GooglePubSubChannelBinding020
    ): domain.bindings.googlepubsub.GooglePubSubChannelBinding020 =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubChannelBinding020](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubChannelBinding020
    ): GooglePubSubChannelBinding020 =
      from._internal
  }
}
trait GooglePubSubMessageStoragePolicyConverter extends PlatformSecrets {
  implicit object GooglePubSubMessageStoragePolicyMatcher
      extends BidirectionalMatcher[
        GooglePubSubMessageStoragePolicy,
        domain.bindings.googlepubsub.GooglePubSubMessageStoragePolicy
      ] {
    override def asClient(
        from: GooglePubSubMessageStoragePolicy
    ): domain.bindings.googlepubsub.GooglePubSubMessageStoragePolicy =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubMessageStoragePolicy](from)
    override def asInternal(
        from: domain.bindings.googlepubsub.GooglePubSubMessageStoragePolicy
    ): GooglePubSubMessageStoragePolicy = from._internal
  }
}
trait GooglePubSubSchemaSettingsConverter extends PlatformSecrets {
  implicit object GooglePubSubSchemaSettingsMatcher
      extends BidirectionalMatcher[
        GooglePubSubSchemaSettings,
        domain.bindings.googlepubsub.GooglePubSubSchemaSettings
      ] {
    override def asClient(from: GooglePubSubSchemaSettings): domain.bindings.googlepubsub.GooglePubSubSchemaSettings =
      platform.wrap[domain.bindings.googlepubsub.GooglePubSubSchemaSettings](from)
    override def asInternal(from: domain.bindings.googlepubsub.GooglePubSubSchemaSettings): GooglePubSubSchemaSettings =
      from._internal
  }
}

trait EndPointConverter extends PlatformSecrets {

  implicit object EndPointMatcher extends BidirectionalMatcher[EndPoint, domain.EndPoint] {
    override def asClient(from: EndPoint): domain.EndPoint   = platform.wrap[domain.EndPoint](from)
    override def asInternal(from: domain.EndPoint): EndPoint = from._internal
  }
}

trait ResourceTypeConverter extends PlatformSecrets {

  implicit object ResourceTypeMatcher extends BidirectionalMatcher[ResourceType, domain.templates.ResourceType] {
    override def asClient(from: ResourceType): domain.templates.ResourceType   = domain.templates.ResourceType(from)
    override def asInternal(from: domain.templates.ResourceType): ResourceType = from._internal
  }
}

trait TraitConverter extends PlatformSecrets {

  implicit object TraitMatcher extends BidirectionalMatcher[Trait, domain.templates.Trait] {
    override def asClient(from: Trait): domain.templates.Trait   = domain.templates.Trait(from)
    override def asInternal(from: domain.templates.Trait): Trait = from._internal
  }
}

trait OrganizationConverter extends PlatformSecrets {

  implicit object OrganizationMatcher extends BidirectionalMatcher[Organization, domain.Organization] {
    override def asClient(from: Organization): domain.Organization   = platform.wrap[domain.Organization](from)
    override def asInternal(from: domain.Organization): Organization = from._internal
  }
}

trait LicenseConverter extends PlatformSecrets {

  implicit object LicenseMatcher extends BidirectionalMatcher[License, domain.License] {
    override def asClient(from: License): domain.License   = platform.wrap[domain.License](from)
    override def asInternal(from: domain.License): License = from._internal
  }
}

trait ParameterConverter extends PlatformSecrets {

  implicit object ParameterMatcher extends BidirectionalMatcher[Parameter, domain.Parameter] {
    override def asClient(from: Parameter): domain.Parameter   = platform.wrap[domain.Parameter](from)
    override def asInternal(from: domain.Parameter): Parameter = from._internal
  }
}

trait ServerConverter extends PlatformSecrets {

  implicit object ServerMatcher extends BidirectionalMatcher[Server, domain.Server] {
    override def asClient(from: Server): domain.Server   = platform.wrap[domain.Server](from)
    override def asInternal(from: domain.Server): Server = from._internal
  }
}

trait CallbackConverter extends PlatformSecrets {

  implicit object CallbackMatcher extends BidirectionalMatcher[Callback, domain.Callback] {
    override def asClient(from: Callback): domain.Callback   = platform.wrap[domain.Callback](from)
    override def asInternal(from: domain.Callback): Callback = from._internal
  }
}

trait EncodingConverter extends PlatformSecrets {

  implicit object EncodingMatcher extends BidirectionalMatcher[Encoding, domain.Encoding] {
    override def asClient(from: Encoding): domain.Encoding   = platform.wrap[domain.Encoding](from)
    override def asInternal(from: domain.Encoding): Encoding = from._internal
  }
}

trait PayloadConverter extends PlatformSecrets {

  implicit object PayloadMatcher extends BidirectionalMatcher[Payload, domain.Payload] {
    override def asClient(from: Payload): domain.Payload   = platform.wrap[domain.Payload](from)
    override def asInternal(from: domain.Payload): Payload = from._internal
  }
}

trait OperationConverter extends PlatformSecrets {

  implicit object OperationMatcher extends BidirectionalMatcher[Operation, domain.Operation] {
    override def asClient(from: Operation): domain.Operation   = platform.wrap[domain.Operation](from)
    override def asInternal(from: domain.Operation): Operation = from._internal
  }

}

trait TagConverter extends PlatformSecrets {

  implicit object TagMatcher extends BidirectionalMatcher[Tag, domain.Tag] {
    override def asClient(from: Tag): domain.Tag   = platform.wrap[domain.Tag](from)
    override def asInternal(from: domain.Tag): Tag = from._internal
  }

}

trait CorrelationIdConverter extends PlatformSecrets {

  implicit object CorrelationIdMatcher extends BidirectionalMatcher[CorrelationId, domain.CorrelationId] {
    override def asClient(from: CorrelationId): domain.CorrelationId   = platform.wrap[domain.CorrelationId](from)
    override def asInternal(from: domain.CorrelationId): CorrelationId = from._internal
  }

}

trait MessageConverter extends PlatformSecrets {

  implicit object ResponseMatcher extends BidirectionalMatcher[Response, domain.Response] {
    override def asClient(from: Response): domain.Response   = platform.wrap[domain.Response](from)
    override def asInternal(from: domain.Response): Response = from._internal
  }

  implicit object RequestMatcher extends BidirectionalMatcher[Request, domain.Request] {
    override def asClient(from: Request): domain.Request   = platform.wrap[domain.Request](from)
    override def asInternal(from: domain.Request): Request = from._internal
  }

  implicit object MessageMatcher extends BidirectionalMatcher[Message, domain.Message] {
    override def asClient(from: Message): domain.Message = from match {
      case req: Request  => RequestMatcher.asClient(req)
      case res: Response => ResponseMatcher.asClient(res)
      case base: Message => new domain.Message(base)
      case _ => // ignore
        null
    }
    override def asInternal(from: domain.Message): Message = from._internal
  }
}

trait ParametrizedSecuritySchemeConverter extends PlatformSecrets {

  implicit object ParametrizedSecuritySchemeMatcher
      extends BidirectionalMatcher[ParametrizedSecurityScheme, domain.security.ParametrizedSecurityScheme] {
    override def asClient(from: ParametrizedSecurityScheme): domain.security.ParametrizedSecurityScheme =
      platform.wrap[domain.security.ParametrizedSecurityScheme](from)

    override def asInternal(from: domain.security.ParametrizedSecurityScheme): ParametrizedSecurityScheme =
      from._internal
  }
}

trait SecuritySchemeConverter extends PlatformSecrets {

  implicit object SecuritySchemeMatcher extends BidirectionalMatcher[SecurityScheme, domain.security.SecurityScheme] {
    override def asClient(from: SecurityScheme): domain.security.SecurityScheme =
      platform.wrap[domain.security.SecurityScheme](from)
    override def asInternal(from: domain.security.SecurityScheme): SecurityScheme = from._internal
  }
}

trait OAuth2FlowConverter extends PlatformSecrets {

  implicit object OAuth2FlowMatcher extends BidirectionalMatcher[OAuth2Flow, domain.security.OAuth2Flow] {
    override def asClient(from: OAuth2Flow): domain.security.OAuth2Flow =
      platform.wrap[domain.security.OAuth2Flow](from)
    override def asInternal(from: domain.security.OAuth2Flow): OAuth2Flow = from._internal
  }
}

trait SecurityRequirementConverter extends PlatformSecrets {

  implicit object SecurityRequirementMatcher
      extends BidirectionalMatcher[SecurityRequirement, domain.security.SecurityRequirement] {
    override def asClient(from: SecurityRequirement): domain.security.SecurityRequirement =
      platform.wrap[domain.security.SecurityRequirement](from)
    override def asInternal(from: domain.security.SecurityRequirement): SecurityRequirement = from._internal
  }
}

trait SettingsConverter extends PlatformSecrets {

  implicit object OAuth1SettingsMatcher extends BidirectionalMatcher[OAuth1Settings, domain.security.OAuth1Settings] {
    override def asClient(from: OAuth1Settings): domain.security.OAuth1Settings   = domain.security.OAuth1Settings(from)
    override def asInternal(from: domain.security.OAuth1Settings): OAuth1Settings = from._internal
  }

  implicit object OAuth2SettingsMatcher extends BidirectionalMatcher[OAuth2Settings, domain.security.OAuth2Settings] {
    override def asClient(from: OAuth2Settings): domain.security.OAuth2Settings   = domain.security.OAuth2Settings(from)
    override def asInternal(from: domain.security.OAuth2Settings): OAuth2Settings = from._internal
  }

  implicit object ApiKeySettingsMatcher extends BidirectionalMatcher[ApiKeySettings, domain.security.ApiKeySettings] {
    override def asClient(from: ApiKeySettings): domain.security.ApiKeySettings   = domain.security.ApiKeySettings(from)
    override def asInternal(from: domain.security.ApiKeySettings): ApiKeySettings = from._internal
  }

  implicit object HttpApiKeySettingsMatcher
      extends BidirectionalMatcher[HttpApiKeySettings, domain.security.HttpApiKeySettings] {
    override def asClient(from: HttpApiKeySettings): domain.security.HttpApiKeySettings =
      domain.security.HttpApiKeySettings(from)
    override def asInternal(from: domain.security.HttpApiKeySettings): HttpApiKeySettings = from._internal
  }

  implicit object HttpSettingsMatcher extends BidirectionalMatcher[HttpSettings, domain.security.HttpSettings] {
    override def asClient(from: HttpSettings): domain.security.HttpSettings   = domain.security.HttpSettings(from)
    override def asInternal(from: domain.security.HttpSettings): HttpSettings = from._internal
  }

  implicit object OpenIdConnectSettingsMatcher
      extends BidirectionalMatcher[OpenIdConnectSettings, domain.security.OpenIdConnectSettings] {
    override def asClient(from: OpenIdConnectSettings): domain.security.OpenIdConnectSettings =
      domain.security.OpenIdConnectSettings(from)
    override def asInternal(from: domain.security.OpenIdConnectSettings): OpenIdConnectSettings = from._internal
  }

  implicit object MutualTLSSettingsMatcher
      extends BidirectionalMatcher[MutualTLSSettings, domain.security.MutualTLSSettings] {
    override def asClient(from: MutualTLSSettings): domain.security.MutualTLSSettings =
      domain.security.MutualTLSSettings(from)
    override def asInternal(from: domain.security.MutualTLSSettings): MutualTLSSettings = from._internal
  }

  implicit object SettingsMatcher extends BidirectionalMatcher[Settings, domain.security.Settings] {
    override def asClient(from: Settings): domain.security.Settings = from match {
      case oauth1: OAuth1Settings        => OAuth1SettingsMatcher.asClient(oauth1)
      case oauth2: OAuth2Settings        => OAuth2SettingsMatcher.asClient(oauth2)
      case apiKey: ApiKeySettings        => ApiKeySettingsMatcher.asClient(apiKey)
      case http: HttpSettings            => HttpSettingsMatcher.asClient(http)
      case openId: OpenIdConnectSettings => OpenIdConnectSettingsMatcher.asClient(openId)
      case base: Settings                => new domain.security.Settings(base)
      case _ => // ignore
        null
    }

    override def asInternal(from: domain.security.Settings): Settings = from._internal
  }
}

trait ScopeConverter extends PlatformSecrets {

  implicit object ScopeMatcher extends BidirectionalMatcher[Scope, domain.security.Scope] {
    override def asClient(from: Scope): domain.security.Scope   = platform.wrap[domain.security.Scope](from)
    override def asInternal(from: domain.security.Scope): Scope = from._internal
  }
}

trait TemplatedLinkConverter extends PlatformSecrets {

  implicit object TemplatedLinkConverter extends BidirectionalMatcher[TemplatedLink, domain.TemplatedLink] {
    override def asClient(from: TemplatedLink): domain.TemplatedLink   = platform.wrap[domain.TemplatedLink](from)
    override def asInternal(from: domain.TemplatedLink): TemplatedLink = from._internal
  }
}

trait AMFConfigurationConverter {
  implicit object AMFConfigurationMatcher extends BidirectionalMatcher[AMFConfiguration, platform.AMFConfiguration] {
    override def asClient(from: AMFConfiguration): platform.AMFConfiguration   = new platform.AMFConfiguration(from)
    override def asInternal(from: platform.AMFConfiguration): AMFConfiguration = from._internal
  }
}

trait AMFLibraryResultConverter {
  implicit object AMFLibraryResultMatcher extends BidirectionalMatcher[AMFLibraryResult, platform.AMFLibraryResult] {
    override def asClient(from: AMFLibraryResult): platform.AMFLibraryResult   = new platform.AMFLibraryResult(from)
    override def asInternal(from: platform.AMFLibraryResult): AMFLibraryResult = from._internal
  }
}

trait AMFDocumentResultConverter {
  implicit object AMFDocumentResultMatcher extends BidirectionalMatcher[AMFDocumentResult, platform.AMFDocumentResult] {
    override def asClient(from: AMFDocumentResult): platform.AMFDocumentResult   = new platform.AMFDocumentResult(from)
    override def asInternal(from: platform.AMFDocumentResult): AMFDocumentResult = from._internal
  }
}

trait APIContractProcessingDataConverter extends PlatformSecrets {
  implicit object APIContractProcessingDataMatcher
      extends BidirectionalMatcher[APIContractProcessingData, document.APIContractProcessingData] {
    override def asClient(from: APIContractProcessingData): document.APIContractProcessingData   = platform.wrap(from)
    override def asInternal(from: document.APIContractProcessingData): APIContractProcessingData = from._internal
  }
}

trait OperationFederationMetadataConverter extends PlatformSecrets {
  implicit object OperationFederationMetadataMatcher
      extends BidirectionalMatcher[OperationFederationMetadata, domain.federation.OperationFederationMetadata] {
    override def asClient(from: OperationFederationMetadata): domain.federation.OperationFederationMetadata =
      platform.wrap[domain.federation.OperationFederationMetadata](from)
    override def asInternal(from: domain.federation.OperationFederationMetadata): OperationFederationMetadata =
      from._internal
  }
}

trait ParameterKeyMappingConverter extends PlatformSecrets {
  implicit object ParameterKeyMappingMatcher
      extends BidirectionalMatcher[ParameterKeyMapping, domain.federation.ParameterKeyMapping] {
    override def asClient(from: ParameterKeyMapping): domain.federation.ParameterKeyMapping =
      platform.wrap[domain.federation.ParameterKeyMapping](from)
    override def asInternal(from: domain.federation.ParameterKeyMapping): ParameterKeyMapping = from._internal
  }
}

trait ComponentModuleConverter extends PlatformSecrets {
  implicit object ComponentModuleMatcher extends BidirectionalMatcher[ComponentModule, document.ComponentModule] {
    override def asClient(from: ComponentModule): document.ComponentModule   = new document.ComponentModule(from)
    override def asInternal(from: document.ComponentModule): ComponentModule = from._internal
  }
}

trait EndpointFederationMetadataConverter extends PlatformSecrets {
  implicit object EndpointFederationMetadataMatcher
      extends BidirectionalMatcher[domain.federation.EndPointFederationMetadata, EndPointFederationMetadata] {
    override def asInternal(from: EndPointFederationMetadata): federation.EndPointFederationMetadata =
      platform.wrap[domain.federation.EndPointFederationMetadata](from)
    override def asClient(from: federation.EndPointFederationMetadata): EndPointFederationMetadata = from._internal
  }
}

trait ParameterFederationMetadataConverter extends PlatformSecrets {
  implicit object ParameterFederationMetadataMatcher
      extends BidirectionalMatcher[domain.federation.ParameterFederationMetadata, ParameterFederationMetadata] {
    override def asInternal(from: ParameterFederationMetadata): federation.ParameterFederationMetadata =
      platform.wrap[domain.federation.ParameterFederationMetadata](from)
    override def asClient(from: federation.ParameterFederationMetadata): ParameterFederationMetadata = from._internal
  }
}
