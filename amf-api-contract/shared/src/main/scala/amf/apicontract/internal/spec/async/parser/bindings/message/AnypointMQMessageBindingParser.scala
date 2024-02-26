package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQMessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object AnypointMQMessageBindingParser extends BindingParser[AnypointMQMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): AnypointMQMessageBinding = {
    val binding = AnypointMQMessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("headers", parseSchema(AnypointMQMessageBindingModel.Headers, binding, _))

    parseBindingVersion(binding, AnypointMQMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "AnypointMQMessageBinding")

    binding
  }
}
