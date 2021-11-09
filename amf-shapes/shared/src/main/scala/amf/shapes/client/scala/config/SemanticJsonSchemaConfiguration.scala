package amf.shapes.client.scala.config

import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.JsonSchemaDialectParsePlugin

object SemanticJsonSchemaConfiguration {

  def predefined(): ShapesConfiguration = {
    // TODO ARM: validate plugin and payload plugin of api?
    ShapesConfiguration
      .predefined()
      .withPlugin(JsonSchemaDialectParsePlugin)
  }
}
