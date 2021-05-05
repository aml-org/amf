package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.execution.BaseExecutionEnvironment
import amf.client.model.domain.AnyShape
import amf.client.render.ShapeRenderOptions
import amf.core.emitter.ShapeRenderOptions.toImmutable
import amf.core.emitter.{ShapeRenderOptions => CoreShapeRenderOptions}
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaSerializer

object ClientJsonSchemaShapeSerializer extends JsonSchemaSerializer {

  def toJsonSchema(element: AnyShape): String = super.toJsonSchema(element, platform.defaultExecutionEnvironment)

  def toJsonSchema(element: AnyShape, exec: BaseExecutionEnvironment): String = super.toJsonSchema(element, exec)

  def buildJsonSchema(element: AnyShape,
                      options: ShapeRenderOptions = ShapeRenderOptions(),
                      exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): String = {
    val coreOptions = CoreShapeRenderOptions(options)
    generateJsonSchema(element, coreOptions, exec)
  }
}
