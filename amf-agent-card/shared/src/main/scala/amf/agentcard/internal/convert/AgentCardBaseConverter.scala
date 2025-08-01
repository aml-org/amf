package amf.agentcard.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentcard.client.platform.{AgentCardConfiguration => ClientAgentCardConfiguration}
import amf.agentcard.client.scala.AgentCardConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentCardBaseConverter
    extends ShapesBaseConverter
    with AgentCardConfigurationConverter

trait AgentCardConfigurationConverter {
  implicit object AgentCardConfigurationMatcher
      extends BidirectionalMatcher[AgentCardConfiguration, ClientAgentCardConfiguration] {
    override def asClient(from: AgentCardConfiguration): ClientAgentCardConfiguration = new ClientAgentCardConfiguration(from)

    override def asInternal(from: ClientAgentCardConfiguration): AgentCardConfiguration = from._internal
  }
}
