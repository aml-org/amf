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
    val binding = AnypointMQChannelBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

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
