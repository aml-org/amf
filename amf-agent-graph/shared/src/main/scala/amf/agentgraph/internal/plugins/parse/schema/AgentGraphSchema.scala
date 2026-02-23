package amf.agentgraph.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema
import org.mulesoft.common.io.Fs

object AgentGraphSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String =
    Fs.syncFile("amf-agent-graph/shared/src/main/resources/schema_agent_graph.json").read().toString

}
