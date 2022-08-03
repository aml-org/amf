package amf.shapes.client.platform.config

import amf.shapes.client.platform.ShapesConfiguration
import amf.shapes.client.scala.config.{JsonSchemaConfiguration => InternalJsonSchemaDocumentConfiguration}

import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("JsonSchemaConfiguration")
object JsonSchemaConfiguration {
  def JsonSchema(): ShapesConfiguration = InternalJsonSchemaDocumentConfiguration.JsonSchema()
}
