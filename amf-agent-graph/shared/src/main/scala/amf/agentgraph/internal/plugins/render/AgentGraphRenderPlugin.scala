package amf.agentgraph.internal.plugins.render

import amf.core.internal.remote.{AgentGraph, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentGraphRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentGraph

}
