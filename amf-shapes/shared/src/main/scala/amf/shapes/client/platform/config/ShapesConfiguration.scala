package amf.shapes.client.platform.config

import amf.aml.client.platform.AMLConfiguration
import amf.shapes.client.scala.config.{ShapesConfiguration => InternalShapesConfiguration}
import amf.aml.internal.convert.VocabulariesClientConverter._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("ShapesConfiguration")
object ShapesConfiguration {

  def predefined(): AMLConfiguration = InternalShapesConfiguration.predefined()
}
