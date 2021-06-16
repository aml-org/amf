package amf.shapes.client.scala.render

import amf.core.client.scala.AMFGraphConfiguration
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.jsonschema.emitter.JsonSchemaSerializer

object JsonSchemaShapeRenderer extends JsonSchemaSerializer {

  override def toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String =
    super.toJsonSchema(element, config)

  def buildJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = generateJsonSchema(element, config)
}
