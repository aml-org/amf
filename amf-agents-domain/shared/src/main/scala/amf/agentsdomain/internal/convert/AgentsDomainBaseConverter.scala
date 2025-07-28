package amf.agentsdomain.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentsdomain.client.platform.{AgentsDomainConfiguration => ClientAgentsDomainConfiguration}
import amf.agentsdomain.client.scala.AgentsDomainConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentsDomainBaseConverter
    extends ShapesBaseConverter
    with AgentsDomainConfigurationConverter

trait AgentsDomainConfigurationConverter {
  implicit object AgentsDomainConfigurationMatcher
      extends BidirectionalMatcher[AgentsDomainConfiguration, ClientAgentsDomainConfiguration] {
    override def asClient(from: AgentsDomainConfiguration): ClientAgentsDomainConfiguration = new ClientAgentsDomainConfiguration(from)

    override def asInternal(from: ClientAgentsDomainConfiguration): AgentsDomainConfiguration = from._internal
  }
}
