package amf.agentnetwork.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentnetwork.client.platform.{AgentNetworkConfiguration => ClientAgentNetworkConfiguration}
import amf.agentnetwork.client.scala.AgentNetworkConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentNetworkBaseConverter
    extends ShapesBaseConverter
    with AgentNetworkConfigurationConverter

trait AgentNetworkConfigurationConverter {
  implicit object AgentNetworkConfigurationMatcher
      extends BidirectionalMatcher[AgentNetworkConfiguration, ClientAgentNetworkConfiguration] {
    override def asClient(from: AgentNetworkConfiguration): ClientAgentNetworkConfiguration = new ClientAgentNetworkConfiguration(from)

    override def asInternal(from: ClientAgentNetworkConfiguration): AgentNetworkConfiguration = from._internal
  }
}
