package amf.mcp.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.mcp.client.platform.{MCPConfiguration => ClientMCPConfiguration}
import amf.mcp.client.scala.MCPConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait MCPBaseConverter
    extends ShapesBaseConverter
    with MCPConfigurationConverter

trait MCPConfigurationConverter {
  implicit object MCPConfigurationMatcher
      extends BidirectionalMatcher[MCPConfiguration, ClientMCPConfiguration] {
    override def asClient(from: MCPConfiguration): ClientMCPConfiguration = new ClientMCPConfiguration(from)

    override def asInternal(from: ClientMCPConfiguration): MCPConfiguration = from._internal
  }
}
