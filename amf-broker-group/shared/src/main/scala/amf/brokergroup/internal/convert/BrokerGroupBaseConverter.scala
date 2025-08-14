package amf.brokergroup.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.brokergroup.client.platform.{BrokerGroupConfiguration => ClientBrokerGroupConfiguration}
import amf.brokergroup.client.scala.BrokerGroupConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait BrokerGroupBaseConverter
    extends ShapesBaseConverter
    with BrokerGroupConfigurationConverter

trait BrokerGroupConfigurationConverter {
  implicit object BrokerGroupConfigurationMatcher
      extends BidirectionalMatcher[BrokerGroupConfiguration, ClientBrokerGroupConfiguration] {
    override def asClient(from: BrokerGroupConfiguration): ClientBrokerGroupConfiguration = new ClientBrokerGroupConfiguration(from)

    override def asInternal(from: ClientBrokerGroupConfiguration): BrokerGroupConfiguration = from._internal
  }
}
