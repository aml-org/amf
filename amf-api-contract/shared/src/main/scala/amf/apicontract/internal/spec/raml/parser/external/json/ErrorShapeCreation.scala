package amf.apicontract.internal.spec.raml.parser.external.json

import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.YNode

trait ErrorShapeCreation {

  protected def errorShape(value: YNode)(implicit ctx: WebApiContext) = {
    val shape = SchemaShape()
    ctx.eh.violation(UnableToParseJsonSchema, shape, "Cannot parse JSON Schema", value.location)
    shape
  }
}
