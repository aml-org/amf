package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.execution.BaseExecutionEnvironment
import amf.client.exported.AMFGraphConfiguration
import amf.client.model.domain.AnyShape
import amf.client.render.ShapeRenderOptions
import amf.core.emitter.ShapeRenderOptions.toImmutable
import amf.core.emitter.{ShapeRenderOptions => CoreShapeRenderOptions}
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaSerializer

object ClientJsonSchemaShapeSerializer extends JsonSchemaSerializer {

  def toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = super.toJsonSchema(element, config)

  def buildJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = {
    generateJsonSchema(element, config)
  }
}
