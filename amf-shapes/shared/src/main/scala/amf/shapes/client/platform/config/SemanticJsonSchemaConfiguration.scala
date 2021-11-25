package amf.shapes.client.platform.config

import amf.shapes.client.platform.ShapesConfiguration
import amf.shapes.client.scala.config.{SemanticJsonSchemaConfiguration => InternalSemanticJsonSchemaConfiguration}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("SemanticJsonSchemaConfiguration")
object SemanticJsonSchemaConfiguration {

  def predefined(): ShapesConfiguration = InternalSemanticJsonSchemaConfiguration.predefined()
}
