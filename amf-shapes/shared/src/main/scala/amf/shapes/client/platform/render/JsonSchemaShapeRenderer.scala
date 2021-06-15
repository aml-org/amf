package amf.shapes.client.platform.render

import amf.core.client.platform.AMFGraphConfiguration
import amf.shapes.client.platform.model.domain.AnyShape
import amf.shapes.client.scala.render.{JsonSchemaShapeRenderer => InternalJsonSchemaShapeRenderer}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object JsonSchemaShapeRenderer {

  def toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String =
    InternalJsonSchemaShapeRenderer.toJsonSchema(element, config)

  def buildJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String =
    InternalJsonSchemaShapeRenderer.buildJsonSchema(element, config)
}
