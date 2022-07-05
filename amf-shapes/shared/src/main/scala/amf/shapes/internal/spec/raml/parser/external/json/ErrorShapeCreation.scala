package amf.shapes.internal.spec.raml.parser.external.json

import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.YNode

trait ErrorShapeCreation {

  protected def errorShape(value: YNode)(implicit ctx: ShapeParserContext) = {
    val shape = SchemaShape()
    ctx.eh.violation(UnableToParseJsonSchema, shape, "Cannot parse JSON Schema", value.location)
    shape
  }
}
