package amf.agentdomain.internal.plugins.render

import amf.core.internal.remote.{AgentDomain, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentDomainRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentDomain

}
