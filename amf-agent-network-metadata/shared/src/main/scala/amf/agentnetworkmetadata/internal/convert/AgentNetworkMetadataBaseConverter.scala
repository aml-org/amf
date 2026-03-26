package amf.agentnetworkmetadata.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentnetworkmetadata.client.platform.{AgentNetworkMetadataConfiguration => ClientAgentNetworkMetadataConfiguration}
import amf.agentnetworkmetadata.client.scala.AgentNetworkMetadataConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentNetworkMetadataBaseConverter
    extends ShapesBaseConverter
    with AgentNetworkMetadataConfigurationConverter

trait AgentNetworkMetadataConfigurationConverter {
  implicit object AgentNetworkMetadataConfigurationMatcher
      extends BidirectionalMatcher[AgentNetworkMetadataConfiguration, ClientAgentNetworkMetadataConfiguration] {
    override def asClient(from: AgentNetworkMetadataConfiguration): ClientAgentNetworkMetadataConfiguration = new ClientAgentNetworkMetadataConfiguration(from)

    override def asInternal(from: ClientAgentNetworkMetadataConfiguration): AgentNetworkMetadataConfiguration = from._internal
  }
}
