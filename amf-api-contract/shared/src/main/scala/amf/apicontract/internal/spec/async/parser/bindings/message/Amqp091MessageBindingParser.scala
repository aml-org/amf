package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091MessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.Amqp091MessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object Amqp091MessageBindingParser extends BindingParser[Amqp091MessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Amqp091MessageBinding = {
    val binding = Amqp091MessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("contentEncoding", Amqp091MessageBindingModel.ContentEncoding in binding)
    map.key("messageType", Amqp091MessageBindingModel.MessageType in binding)
    parseBindingVersion(binding, Amqp091MessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "amqpMessageBinding")

    binding
  }
}
