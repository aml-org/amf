package amf.othercard.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.{JsonSchemaBasedSpecSchema, JsonSchemaBasedSpecSchemaLoader}

object OtherCardSchemaLoader extends JsonSchemaBasedSpecSchemaLoader {

  override protected def schemaProvider: JsonSchemaBasedSpecSchema = OtherCardSchema

}
