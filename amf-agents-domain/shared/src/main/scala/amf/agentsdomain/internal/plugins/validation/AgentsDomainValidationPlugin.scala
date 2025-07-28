package amf.agentsdomain.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agentsdomain.internal.plugins.parse.schema.AgentsDomainSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentsDomainValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentsDomainSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENTS_DOMAIN
}

object AgentsDomainValidationPlugin {
  def apply(): AgentsDomainValidationPlugin = new AgentsDomainValidationPlugin()
}
