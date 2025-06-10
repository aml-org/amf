package amf.mcp.client.platform

import amf.aml.client.platform.AMLBaseUnitClient
import amf.core.client.platform.model.document.BaseUnit
import amf.core.client.platform.validation.AMFValidationReport
import amf.mcp.client.scala.{MCPBaseUnitClient => InternalMCPBaseUnitClient}
import amf.mcp.internal.convert.MCPClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class MCPBaseUnitClient private[amf] (private val _internal: InternalMCPBaseUnitClient)
    extends AMLBaseUnitClient(_internal) {

  def syncValidate(baseUnit: BaseUnit): AMFValidationReport = _internal.syncValidate(baseUnit._internal)
}
