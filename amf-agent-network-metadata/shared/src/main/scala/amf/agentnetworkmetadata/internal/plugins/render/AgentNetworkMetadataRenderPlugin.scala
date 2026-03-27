package amf.agentnetworkmetadata.internal.plugins.render

import amf.core.internal.remote.{AgentNetworkMetadata, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentNetworkMetadataRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentNetworkMetadata

}
