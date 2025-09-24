package amf.agentnetwork.internal.plugins.render

import amf.core.internal.remote.{AgentNetwork, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentNetworkRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentNetwork

}
