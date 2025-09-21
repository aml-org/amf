package amf.agentmetadata.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentMetadataSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String =
    """
      |{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |  "title": "Agent Metadata Specification",
      |  "@context": {
      |    "@type": [
      |      "http://anypoint.com/vocabs/agents#AgentMetadata"
      |    ],
      |    "@base": "http://anypoint.com/vocabs/agents#"
      |  },
      |  "$ref": "#/definitions/AgentMetadata",
      |  "definitions": {
      |    "GAV": {
      |      "type": "object",
      |      "properties": {
      |        "groupId": {
      |          "type": "string"
      |        },
      |        "assetId": {
      |          "type": "string"
      |        },
      |        "version": {
      |          "type": "string"
      |        }
      |      }
      |    },
      |    "AgentMetadata": {
      |      "type": "object",
      |      "additionalProperties": true,
      |      "properties": {
      |        "protocol": {
      |          "type": "string",
      |          "enum": [
      |            "a2a",
      |            "other"
      |          ]
      |        },
      |        "platform": {
      |          "type": "string"
      |        },
      |        "kind": {
      |          "type": "string"
      |        },
      |        "connections": {
      |          "type": "array",
      |          "items": {
      |            "type": "object",
      |            "oneOf": [
      |              {
      |                "$ref": "#/definitions/AgentLink"
      |              },
      |              {
      |                "$ref": "#/definitions/MCPLink"
      |              },
      |              {
      |                "$ref": "#/definitions/LLMLink"
      |              }
      |            ]
      |          }
      |        },
      |        "provenance": {
      |          "@context": {
      |            "@type": [
      |              "http://anypoint.com/vocabs/agents#Provenance"
      |            ],
      |            "@base": "http://anypoint.com/vocabs/agents#"
      |          },
      |          "type": "object",
      |          "required": [
      |            "kind"
      |          ],
      |          "properties": {
      |            "kind": {
      |              "type": "string"
      |            },
      |            "metadata": {
      |              "type": "object",
      |              "additionalProperties": true
      |            }
      |          },
      |          "additionalProperties": false
      |        }
      |      }
      |    },
      |    "AgentLink": {
      |      "@context": {
      |        "@type": [
      |          "http://anypoint.com/vocabs/agents/link#Agent"
      |        ],
      |        "@base": "http://anypoint.com/vocabs/agents/link#"
      |      },
      |      "type": "object",
      |      "additionalProperties": false,
      |      "properties": {
      |        "kind": {
      |          "type": "string",
      |          "const": "agent"
      |        },
      |        "ref": {
      |          "$ref": "#/definitions/GAV"
      |        }
      |      }
      |    },
      |    "MCPLink": {
      |      "@context": {
      |        "@type": [
      |          "http://anypoint.com/vocabs/agents/link#MCP"
      |        ],
      |        "@base": "http://anypoint.com/vocabs/agents/link#"
      |      },
      |      "type": "object",
      |      "additionalProperties": false,
      |      "properties": {
      |        "kind": {
      |          "type": "string",
      |          "const": "mcp"
      |        },
      |        "ref": {
      |          "$ref": "#/definitions/GAV"
      |        },
      |        "allowed": {
      |          "type": "array",
      |          "items": {
      |            "type": "string"
      |          }
      |        }
      |      }
      |    },
      |    "LLMLink": {
      |      "@context": {
      |        "@type": [
      |          "http://anypoint.com/vocabs/agents/link#LLM"
      |        ],
      |        "@base": "http://anypoint.com/vocabs/agents/link#"
      |      },
      |      "type": "object",
      |      "additionalProperties": false,
      |      "properties": {
      |        "kind": {
      |          "type": "string",
      |          "const": "llm"
      |        },
      |        "ref": {
      |          "$ref": "#/definitions/GAV"
      |        }
      |      }
      |    }
      |  }
      |}
      |""".stripMargin

}
