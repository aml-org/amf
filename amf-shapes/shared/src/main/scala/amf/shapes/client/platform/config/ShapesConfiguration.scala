package amf.shapes.client.platform.config

import amf.aml.client.platform.{AMLConfiguration, BaseAMLConfiguration}
import amf.shapes.client.scala.config.{ShapesConfiguration => InternalShapesConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.aml.internal.convert.VocabulariesClientConverter._

@JSExportAll
@JSExportTopLevel("ShapesConfiguration")
object ShapesConfiguration {

  def predefined(): AMLConfiguration = InternalShapesConfiguration.predefined()
}
