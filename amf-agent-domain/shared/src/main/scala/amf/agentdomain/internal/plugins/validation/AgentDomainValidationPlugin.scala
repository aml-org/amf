package amf.agentdomain.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agentdomain.internal.plugins.parse.schema.AgentDomainSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentDomainValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentDomainSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_DOMAIN
}

object AgentDomainValidationPlugin {
  def apply(): AgentDomainValidationPlugin = new AgentDomainValidationPlugin()
}
