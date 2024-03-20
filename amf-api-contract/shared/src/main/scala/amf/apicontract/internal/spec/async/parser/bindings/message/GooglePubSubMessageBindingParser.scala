package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{GooglePubSubMessageBinding, GooglePubSubSchemaDefinition}
import amf.apicontract.internal.metamodel.domain.bindings.{GooglePubSubMessageBindingModel, GooglePubSubSchemaDefinitionModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object GooglePubSubMessageBindingParser extends BindingParser[GooglePubSubMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): GooglePubSubMessageBinding = {
    val binding = GooglePubSubMessageBinding(Annotations(entry))
    val map = entry.value.as[YMap]

    map.key("attributes", GooglePubSubMessageBindingModel.Attributes in binding)
    map.key("orderingKey", GooglePubSubMessageBindingModel.OrderingKey in binding)
    parseSchemaDefinition(binding, map)

    parseBindingVersion(binding, GooglePubSubMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "GooglePubSubMessageBinding")
    binding
  }

  private def parseSchemaDefinition(binding: GooglePubSubMessageBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit ={
    map.key("schema", { entry =>
      val schema = GooglePubSubSchemaDefinition(Annotations(entry.value))
      val schemaMap = entry.value.as[YMap]

      schemaMap.key("name", GooglePubSubSchemaDefinitionModel.Name in schema)
      schemaMap.key("fieldType", GooglePubSubSchemaDefinitionModel.FieldType in schema)

      ctx.closedShape(schema, schemaMap, "GooglePubSubMessageSchema")
      binding.setWithoutId(GooglePubSubMessageBindingModel.Schema, schema, Annotations(entry))
    }
   )
  }
}

