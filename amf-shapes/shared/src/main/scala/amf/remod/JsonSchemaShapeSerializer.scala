package amf.remod

import amf.client.remod.AMFGraphConfiguration
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaSerializer
import amf.plugins.domain.shapes.models.AnyShape

object JsonSchemaShapeSerializer extends JsonSchemaSerializer {

  def toJsonSchema(element: AnyShape): String = super.toJsonSchema(element, AMFGraphConfiguration.predefined())

  override def toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String =
    super.toJsonSchema(element, config)

  def buildJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String =
    generateJsonSchema(element, config)
}
