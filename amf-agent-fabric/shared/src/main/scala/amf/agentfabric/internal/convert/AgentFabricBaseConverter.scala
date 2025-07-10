package amf.agentfabric.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentfabric.client.platform.{AgentFabricConfiguration => ClientAgentFabricConfiguration}
import amf.agentfabric.client.scala.AgentFabricConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentFabricBaseConverter
    extends ShapesBaseConverter
    with AgentFabricConfigurationConverter

trait AgentFabricConfigurationConverter {
  implicit object AgentFabricConfigurationMatcher
      extends BidirectionalMatcher[AgentFabricConfiguration, ClientAgentFabricConfiguration] {
    override def asClient(from: AgentFabricConfiguration): ClientAgentFabricConfiguration = new ClientAgentFabricConfiguration(from)

    override def asInternal(from: ClientAgentFabricConfiguration): AgentFabricConfiguration = from._internal
  }
}
