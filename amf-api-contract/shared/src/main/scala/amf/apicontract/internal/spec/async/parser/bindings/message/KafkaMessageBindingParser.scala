package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.kafka.{
  KafkaMessageBinding,
  KafkaMessageBinding010,
  KafkaMessageBinding030
}
import amf.apicontract.internal.metamodel.domain.bindings.{KafkaMessageBinding030Model, KafkaMessageBindingModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec.ASYNC20
import org.yaml.model.{YMap, YMapEntry}

object KafkaMessageBindingParser extends BindingParser[KafkaMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaMessageBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "KafkaMessageBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0 in 2.0 or 0.3.0 in async 2.1+
    val binding: KafkaMessageBinding = bindingVersion match {
      case "0.3.0" | "0.4.0" | "0.5.0" | "latest" => KafkaMessageBinding030(Annotations(entry))
      case "0.1.0" | "0.2.0"                      => KafkaMessageBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = ctx.specSettings.spec match {
          case ASYNC20 => KafkaMessageBinding010(Annotations(entry))
          case _       => KafkaMessageBinding030(Annotations(entry))
        }
        invalidBindingVersion(defaultBinding, invalidVersion, "Kafka Binding", warning = true)
        defaultBinding
    }

    val map = entry.value.as[YMap]

    bindingVersion match {
      case "0.2.0" | "0.3.0" | "0.4.0" | "0.5.0" | "latest" => // 0.2.0+ support references to schemas in the key field
        map.key("key").foreach { entry =>
          ctx.link(entry.value) match {
            case Left(fullRef) => handleRef(fullRef, "schemas", entry, KafkaMessageBindingModel.MessageKey, binding)
            case Right(_)      => parseSchema(KafkaMessageBindingModel.MessageKey, binding, entry)
          }
        }
      case _ => // any other binding version defaults to 0.1.0
        map.key("key", entry => parseSchema(KafkaMessageBindingModel.MessageKey, binding, entry))
    }

    bindingVersion match {
      case "0.3.0" | "0.4.0" | "0.5.0" | "latest" => // 0.2.0+ support references to schemas in the key field
        map.key("schemaIdLocation", KafkaMessageBinding030Model.SchemaIdLocation in binding)
        map.key("schemaIdPayloadEncoding", KafkaMessageBinding030Model.SchemaIdPayloadEncoding in binding)
        map.key("schemaLookupStrategy", KafkaMessageBinding030Model.SchemaLookupStrategy in binding)
        ctx.closedShape(binding, map, "kafkaMessageBinding030")
      case _ =>
        ctx.closedShape(binding, map, "kafkaMessageBinding010")
    }

    parseBindingVersion(binding, KafkaMessageBindingModel.BindingVersion, map)

    binding
  }
}
