package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQMessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object IBMMQMessageBindingParser extends BindingParser[IBMMQMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): IBMMQMessageBinding = {
    val binding = IBMMQMessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("type", IBMMQMessageBindingModel.MessageType in binding)
    map.key("headers", IBMMQMessageBindingModel.Headers in binding)
    map.key("description", IBMMQMessageBindingModel.Description in binding)
    map.key("expiry", IBMMQMessageBindingModel.Expiry in binding)

    parseBindingVersion(binding, IBMMQMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "IBMMQMessageBinding")

    binding
  }
}
