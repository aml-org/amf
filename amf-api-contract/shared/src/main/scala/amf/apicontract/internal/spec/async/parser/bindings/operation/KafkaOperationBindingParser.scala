package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaOperationBinding
import amf.apicontract.internal.metamodel.domain.bindings.KafkaOperationBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object KafkaOperationBindingParser extends BindingParser[KafkaOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): KafkaOperationBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "KafkaOperationBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0
    val binding: KafkaOperationBinding = bindingVersion match {
      case "0.1.0" | "0.2.0" | "0.3.0" | "0.4.0" | "latest" => KafkaOperationBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = KafkaOperationBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Kafka Binding", warning = true)
        defaultBinding
    }

    val map = entry.value.as[YMap]

    bindingVersion match {
      case "0.4.0" | "latest" => // 0.4.0 onwards support references to schemas in the groupId and clientId fields
        map.key("groupId").foreach { entry =>
          ctx.link(entry.value) match {
            case Left(fullRef) => handleRef(fullRef, "schemas", entry, KafkaOperationBindingModel.GroupId, binding)
            case Right(_)      => parseSchema(KafkaOperationBindingModel.GroupId, binding, entry)
          }
        }
        map.key("clientId").foreach { entry =>
          ctx.link(entry.value) match {
            case Left(fullRef) => handleRef(fullRef, "schemas", entry, KafkaOperationBindingModel.GroupId, binding)
            case Right(_)      => parseSchema(KafkaOperationBindingModel.ClientId, binding, entry)
          }
        }
      case _ => // any other binding version defaults to 0.1.0
        map.key("groupId", entry => parseSchema(KafkaOperationBindingModel.GroupId, binding, entry))
        map.key("clientId", entry => parseSchema(KafkaOperationBindingModel.ClientId, binding, entry))
    }

    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaOperationBinding")

    binding
  }
}
