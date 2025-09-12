package amf.agentmetadata.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentMetadataSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String =
    """
      |{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |  "definitions": {}
      |}
      |""".stripMargin

}
