package amf.shapes.client.platform.config

import amf.aml.client.platform.AMLConfiguration
import amf.aml.internal.convert.VocabulariesClientConverter._
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.config.{SemanticJsonSchemaConfiguration => InternalSemanticJsonSchemaConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("SemanticJsonSchemaConfiguration")
object SemanticJsonSchemaConfiguration {

  def predefined(): ShapesConfiguration = InternalSemanticJsonSchemaConfiguration.predefined()
}
