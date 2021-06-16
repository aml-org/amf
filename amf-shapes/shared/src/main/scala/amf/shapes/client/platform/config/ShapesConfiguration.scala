package amf.shapes.client.platform.config

import amf.client.exported.AMLConfiguration
import amf.shapes.client.scala.config.{ShapesConfiguration => InternalShapesConfiguration}
import amf.client.convert.VocabulariesClientConverter._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object ShapesConfiguration {

  def predefined(): AMLConfiguration = InternalShapesConfiguration.predefined()
}
