package amf.agentmetadata.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentmetadata.client.platform.{AgentMetadataConfiguration => ClientAgentMetadataConfiguration}
import amf.agentmetadata.client.scala.AgentMetadataConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentMetadataBaseConverter
    extends ShapesBaseConverter
    with AgentMetadataConfigurationConverter

trait AgentMetadataConfigurationConverter {
  implicit object AgentMetadataConfigurationMatcher
      extends BidirectionalMatcher[AgentMetadataConfiguration, ClientAgentMetadataConfiguration] {
    override def asClient(from: AgentMetadataConfiguration): ClientAgentMetadataConfiguration = new ClientAgentMetadataConfiguration(from)

    override def asInternal(from: ClientAgentMetadataConfiguration): AgentMetadataConfiguration = from._internal
  }
}
