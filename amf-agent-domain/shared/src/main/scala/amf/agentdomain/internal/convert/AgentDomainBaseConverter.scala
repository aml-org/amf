package amf.agentdomain.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.agentdomain.client.platform.{AgentDomainConfiguration => ClientAgentDomainConfiguration}
import amf.agentdomain.client.scala.AgentDomainConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait AgentDomainBaseConverter
    extends ShapesBaseConverter
    with AgentDomainConfigurationConverter

trait AgentDomainConfigurationConverter {
  implicit object AgentDomainConfigurationMatcher
      extends BidirectionalMatcher[AgentDomainConfiguration, ClientAgentDomainConfiguration] {
    override def asClient(from: AgentDomainConfiguration): ClientAgentDomainConfiguration = new ClientAgentDomainConfiguration(from)

    override def asInternal(from: ClientAgentDomainConfiguration): AgentDomainConfiguration = from._internal
  }
}
