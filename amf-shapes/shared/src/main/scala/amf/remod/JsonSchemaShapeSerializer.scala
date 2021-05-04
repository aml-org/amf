package amf.remod

import amf.client.execution.BaseExecutionEnvironment
import amf.core.emitter.ShapeRenderOptions
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaSerializer
import amf.plugins.domain.shapes.models.AnyShape

object JsonSchemaShapeSerializer extends JsonSchemaSerializer {

  def toJsonSchema(element: AnyShape): String = super.toJsonSchema(element, platform.defaultExecutionEnvironment)

  override def toJsonSchema(element: AnyShape, exec: BaseExecutionEnvironment): String =
    super.toJsonSchema(element, exec)

  def buildJsonSchema(element: AnyShape,
                      options: ShapeRenderOptions = ShapeRenderOptions(),
                      exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): String =
    generateJsonSchema(element, options, exec)
}
