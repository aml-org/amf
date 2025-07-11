package amf.agentfabric.internal.plugins.render

import amf.core.internal.remote.{AgentFabric, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentFabricRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentFabric

}
