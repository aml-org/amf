package amf.mcp.client.scala

import amf.core.client.common.validation.{ProfileNames, ValidationMode}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.AMFParser
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Mimes
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.mcp.internal.plugins.validation.MCPValidationHelper
import amf.shapes.client.scala.ShapesBaseUnitClient
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.plugins.render.JsonLDInstanceRenderHelper

import scala.concurrent.{ExecutionContext, Future}

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class MCPBaseUnitClient private[amf] (override protected val configuration: MCPConfiguration)
    extends ShapesBaseUnitClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: MCPConfiguration = configuration

  def syncValidate(baseUnit: BaseUnit): AMFValidationReport = {
    MCPValidationHelper.validateMCPInstance(baseUnit.asInstanceOf[JsonLDInstanceDocument])
  }
}
