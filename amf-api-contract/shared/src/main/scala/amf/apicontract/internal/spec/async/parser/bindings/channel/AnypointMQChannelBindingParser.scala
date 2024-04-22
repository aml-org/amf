package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQChannelBinding
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQChannelBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object AnypointMQChannelBindingParser extends BindingParser[AnypointMQChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): AnypointMQChannelBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "AnypointMQMessageBinding", ctx.specSettings.spec)

    val binding: AnypointMQChannelBinding = bindingVersion match {
      case "0.1.0" | "latest" => AnypointMQChannelBinding(Annotations(entry))
      case "0.0.1"            => AnypointMQChannelBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = AnypointMQChannelBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "AnypointMQ Binding")
        defaultBinding
    }

    map.key("destination") match {
      case Some(value) => Some(value).foreach(AnypointMQChannelBindingModel.Destination in binding)
      case None        => setDefaultValue(binding, AnypointMQChannelBindingModel.Destination, AmfScalar("default"))
    }

    map.key("destinationType") match {
      case Some(value) => Some(value).foreach(AnypointMQChannelBindingModel.DestinationType in binding)
      case None        => setDefaultValue(binding, AnypointMQChannelBindingModel.DestinationType, AmfScalar("queue"))
    }

    parseBindingVersion(binding, AnypointMQChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "AnypointMQChannelBinding")

    binding
  }
}
