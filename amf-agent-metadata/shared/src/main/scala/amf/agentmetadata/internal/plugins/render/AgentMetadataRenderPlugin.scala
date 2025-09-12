package amf.agentmetadata.internal.plugins.render

import amf.core.internal.remote.{AgentMetadata, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object AgentMetadataRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = AgentMetadata

}
