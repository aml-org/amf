package amf.mcp.client.platform

import amf.mcp.client.scala.{MCPBaseUnitClient => InternalMCPBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class MCPBaseUnitClient private[amf] (private val _internal: InternalMCPBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
