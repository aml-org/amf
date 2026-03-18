package amf.agenticnetwork.internal.plugins.render

import amf.core.internal.remote.{AgenticNetwork, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgenticNetworkRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgenticNetwork

}
