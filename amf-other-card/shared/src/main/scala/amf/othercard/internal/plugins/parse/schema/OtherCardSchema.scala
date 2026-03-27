package amf.othercard.internal.plugins.parse.schema

import amf.othercard.internal.spec.OtherCardSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object OtherCardSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = OtherCardSchemaContent.content

}
