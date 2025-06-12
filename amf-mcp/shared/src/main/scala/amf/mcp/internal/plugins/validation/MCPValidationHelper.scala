package amf.mcp.internal.plugins.validation

import amf.core.client.common.validation.{ProfileNames, ValidationMode}
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Mimes
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.internal.plugins.render.JsonLDInstanceRenderHelper

object MCPValidationHelper {
  private lazy val config = ShapesConfiguration.predefined()

  def validateMCPInstance(instanceUnit: JsonLDInstanceDocument): AMFValidationReport = {
    val encoded = instanceUnit.encodes.head.asInstanceOf[JsonLDObject]
    val content = JsonLDInstanceRenderHelper.renderToJson(encoded)
    val results = config
      .elementClient()
      .payloadValidatorFor(MCPSchemaLoader.schema, Mimes.`application/json`, ValidationMode.StrictValidationMode)
      .syncValidate(content)
      .results
      .distinct
    val report = AMFValidationReport(instanceUnit.location().getOrElse(""), ProfileNames.MCP, results)
    report
  }
}
