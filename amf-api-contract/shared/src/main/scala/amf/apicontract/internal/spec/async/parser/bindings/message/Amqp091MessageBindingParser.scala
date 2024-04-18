package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091MessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.Amqp091MessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.UnsupportedBindingVersionWarning
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object Amqp091MessageBindingParser extends BindingParser[Amqp091MessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Amqp091MessageBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "Amqp091MessageBinding", ctx.specSettings.spec)

    // bindingVersion is either well defined or defaults to 0.1.0
    val binding: Amqp091MessageBinding = bindingVersion match {
      case "0.1.0" | "0.2.0" | "0.3.0" | "latest" => Amqp091MessageBinding(Annotations(entry))
      case invalidVersion =>
        ctx.eh.warning(
          UnsupportedBindingVersionWarning,
          Amqp091MessageBinding(Annotations(entry)),
          Some("bindingVersion"),
          s"Version $invalidVersion is not supported in an Amqp091ChannelBinding",
          entry.value.location
        )
        Amqp091MessageBinding(Annotations(entry))
    }

    val map = entry.value.as[YMap]

    map.key("contentEncoding", Amqp091MessageBindingModel.ContentEncoding in binding)
    map.key("messageType", Amqp091MessageBindingModel.MessageType in binding)
    parseBindingVersion(binding, Amqp091MessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "amqpMessageBinding")

    binding
  }
}
