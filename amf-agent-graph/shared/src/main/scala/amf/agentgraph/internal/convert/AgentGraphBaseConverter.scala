package amf.agentgraph.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentgraph.client.platform.{AgentGraphConfiguration => ClientAgentGraphConfiguration}
import amf.agentgraph.client.scala.AgentGraphConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentGraphBaseConverter
    extends ShapesBaseConverter
    with AgentGraphConfigurationConverter

trait AgentGraphConfigurationConverter {
  implicit object AgentGraphConfigurationMatcher
      extends BidirectionalMatcher[AgentGraphConfiguration, ClientAgentGraphConfiguration] {
    override def asClient(from: AgentGraphConfiguration): ClientAgentGraphConfiguration = new ClientAgentGraphConfiguration(from)

    override def asInternal(from: ClientAgentGraphConfiguration): AgentGraphConfiguration = from._internal
  }
}
