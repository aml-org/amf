package amf.agenticnetwork.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agenticnetwork.client.platform.{AgenticNetworkConfiguration => ClientAgenticNetworkConfiguration}
import amf.agenticnetwork.client.scala.AgenticNetworkConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgenticNetworkBaseConverter
    extends ShapesBaseConverter
    with AgenticNetworkConfigurationConverter

trait AgenticNetworkConfigurationConverter {
  implicit object AgenticNetworkConfigurationMatcher
      extends BidirectionalMatcher[AgenticNetworkConfiguration, ClientAgenticNetworkConfiguration] {
    override def asClient(from: AgenticNetworkConfiguration): ClientAgenticNetworkConfiguration = new ClientAgenticNetworkConfiguration(from)

    override def asInternal(from: ClientAgenticNetworkConfiguration): AgenticNetworkConfiguration = from._internal
  }
}
