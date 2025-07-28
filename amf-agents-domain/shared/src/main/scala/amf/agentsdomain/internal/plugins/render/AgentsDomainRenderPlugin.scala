package amf.agentsdomain.internal.plugins.render

import amf.core.internal.remote.{AgentsDomain, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentsDomainRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentsDomain

}
