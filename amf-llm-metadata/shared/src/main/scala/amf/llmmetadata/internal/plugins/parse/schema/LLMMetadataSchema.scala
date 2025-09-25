package amf.llmmetadata.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object LLMMetadataSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String =
    """{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |  "title": "LLM Metadata Classifier Schema",
      |  "$ref": "#/definitions/LLMMetadataClassifier",
      |  "definitions": {
      |    "LLMMetadataClassifier": {
      |      "type": "object",
      |      "required": [
      |        "platform",
      |        "policyRef"
      |      ],
      |      "properties": {
      |        "platform": {
      |          "type": "string"
      |        },
      |        "policyRef": {
      |          "type": "object",
      |          "required": [
      |            "groupId",
      |            "assetId",
      |            "version"
      |          ],
      |          "properties": {
      |            "groupId": {
      |              "type": "string"
      |            },
      |            "assetId": {
      |              "type": "string"
      |            },
      |            "version": {
      |              "type": "string"
      |            }
      |          }
      |        }
      |      }
      |    }
      |  }
      |}
      |""".stripMargin

}
