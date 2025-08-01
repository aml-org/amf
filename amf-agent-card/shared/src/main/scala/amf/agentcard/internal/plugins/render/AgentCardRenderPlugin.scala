package amf.agentcard.internal.plugins.render

import amf.core.internal.remote.{AgentCard, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentCardRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentCard

}
