package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.googlepubsub.{
  GooglePubSubMessageBinding,
  GooglePubSubMessageBinding010,
  GooglePubSubMessageBinding020,
  GooglePubSubSchemaDefinition010,
  GooglePubSubSchemaDefinition020
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  GooglePubSubMessageBindingModel,
  GooglePubSubSchemaDefinition010Model,
  GooglePubSubSchemaDefinitionModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object GooglePubSubMessageBindingParser extends BindingParser[GooglePubSubMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): GooglePubSubMessageBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "GooglePubSubMessageBinding", ctx.specSettings.spec)

    val binding = bindingVersion match {
      case "0.2.0" | "latest" => GooglePubSubMessageBinding020(Annotations(entry))
      case "0.1.0"            => GooglePubSubMessageBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = GooglePubSubMessageBinding010(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "GooglePubSub Message Binding")
        defaultBinding
    }

    map.key(
      "attributes",
      entry => {
        val valueDataNode = DataNodeParser(entry.value).parse()
        binding.setWithoutId(GooglePubSubMessageBindingModel.Attributes, valueDataNode, Annotations(entry))
      }
    )
    map.key("orderingKey", GooglePubSubMessageBindingModel.OrderingKey in binding)
    parseSchemaDefinition(binding, map, bindingVersion)

    parseBindingVersion(binding, GooglePubSubMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "GooglePubSubMessageBinding")
    binding
  }

  private def parseSchemaDefinition(binding: GooglePubSubMessageBinding, map: YMap, bindingVersion: String)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key(
      "schema",
      { entry =>
        val schemaMap = entry.value.as[YMap]

        val schema = bindingVersion match {
          case "0.2.0" | "latest" => GooglePubSubSchemaDefinition020(Annotations(entry.value))
          case _                  => GooglePubSubSchemaDefinition010(Annotations(entry.value))
        }

        schemaMap.key("name", GooglePubSubSchemaDefinitionModel.Name in schema)

        bindingVersion match {
          case "0.2.0" | "latest" =>
            GooglePubSubMessageBinding020(Annotations(entry.value))
            ctx.closedShape(schema, schemaMap, "GooglePubSubMessageSchema020")
          case _ =>
            GooglePubSubMessageBinding010(Annotations(entry.value))
            schemaMap.key("type", GooglePubSubSchemaDefinition010Model.FieldType in schema)
            ctx.closedShape(schema, schemaMap, "GooglePubSubMessageSchema010")
        }

        binding.setWithoutId(GooglePubSubMessageBindingModel.Schema, schema, Annotations(entry))
      }
    )
  }
}
