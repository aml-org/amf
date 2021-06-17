package amf.shapes.client.platform.render

import amf.core.client.platform.AMFGraphConfiguration
import amf.shapes.client.platform.model.domain.AnyShape
import amf.shapes.client.scala.render.{RamlShapeRenderer => InternalRamlShapeRenderer}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("RamlShapeRenderer")
object RamlShapeRenderer {

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  def toRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): String =
    InternalRamlShapeRenderer.toRamlDatatype(element, config)
}
