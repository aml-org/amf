package amf.remod

import amf.client.convert.shapeconverters.ShapeClientConverters._
import amf.client.model.domain.AnyShape
import amf.core.client.scala.AMFGraphConfiguration
import amf.plugins.document.apicontract.parser.spec.common.JsonSchemaSerializer

object ClientJsonSchemaShapeSerializer extends JsonSchemaSerializer {

  def toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = super.toJsonSchema(element, config)

  def buildJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = {
    generateJsonSchema(element, config)
  }
}
