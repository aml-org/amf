package amf.agentgraph.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentGraphSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String =
    """
      |{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |  "type": "object",
      |  "required": [ "agent-graph" ],
      |  "properties": {
      |    "agent-graph": {
      |      "type": "string",
      |      "pattern": "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
      |    }
      |  }
      |}
      |""".stripMargin

}
