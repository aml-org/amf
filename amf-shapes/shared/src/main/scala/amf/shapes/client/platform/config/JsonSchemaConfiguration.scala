package amf.shapes.client.platform.config

import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.config.{JsonSchemaConfiguration => InternalJsonSchemaDocumentConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("JsonSchemaConfiguration")
object JsonSchemaConfiguration {
  def JsonSchema(): ShapesConfiguration = InternalJsonSchemaDocumentConfiguration.JsonSchema()
}
