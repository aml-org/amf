package amf.shapes.client.scala.config

import amf.aml.client.scala.AMLConfiguration
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.JsonSchemaDialectParsePlugin

object SemanticJsonSchemaConfiguration {

  def predefined(): AMLConfiguration = {
    // TODO ARM: validate plugin and payload plugin of api?
    ShapesConfiguration
      .predefined()
      .withPlugin(JsonSchemaDialectParsePlugin)
  }
}