package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.amqp.{
  Amqp091OperationBinding,
  Amqp091OperationBinding010,
  Amqp091OperationBinding030
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  Amqp091OperationBinding010Model,
  Amqp091OperationBindingModel,
  KafkaOperationBindingModel
}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.UnsupportedBindingVersionWarning
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object Amqp091OperationBindingParser extends BindingParser[Amqp091OperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Amqp091OperationBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "Amqp091OperationBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0
    val binding: Amqp091OperationBinding = bindingVersion match {
      case "0.3.0" | "latest" => Amqp091OperationBinding030(Annotations(entry))
      case "0.1.0" | "0.2.0"  => Amqp091OperationBinding010(Annotations(entry))
      case invalidVersion =>
        ctx.eh.warning(
          UnsupportedBindingVersionWarning,
          Amqp091OperationBinding010(Annotations(entry)),
          Some("bindingVersion"),
          s"Version $invalidVersion is not supported in an Amqp091ChannelBinding",
          entry.value.location
        )
        Amqp091OperationBinding010(Annotations(entry))
    }
    val map = entry.value.as[YMap]

    map.key("expiration", Amqp091OperationBindingModel.Expiration in binding)
    map.key("userId", Amqp091OperationBindingModel.UserId in binding)
    map.key("cc", Amqp091OperationBindingModel.CC in binding)
    map.key("priority", Amqp091OperationBindingModel.Priority in binding)
    map.key("deliveryMode", Amqp091OperationBindingModel.DeliveryMode in binding)
    map.key("mandatory", Amqp091OperationBindingModel.Mandatory in binding)
    map.key("bcc", Amqp091OperationBindingModel.BCC in binding)
    map.key("timestamp", Amqp091OperationBindingModel.Timestamp in binding)
    map.key("ack", Amqp091OperationBindingModel.Ack in binding)

    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    bindingVersion match {
      case "0.3.0" =>
        ctx.closedShape(binding, map, "amqpOperationBinding030")
      case _ =>
        map.key("replyTo", Amqp091OperationBinding010Model.ReplyTo in binding)
        ctx.closedShape(binding, map, "amqpOperationBinding010")
    }

    binding
  }
}
