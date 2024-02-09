package amf.apicontract.internal.spec.async.parser.bindings.channel

import amf.apicontract.client.scala.model.domain.bindings.websockets.WebSocketsChannelBinding
import amf.apicontract.internal.metamodel.domain.bindings.WebSocketsChannelBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object WebSocketsChannelBindingParser extends BindingParser[WebSocketsChannelBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): WebSocketsChannelBinding = {
    val binding = WebSocketsChannelBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("method", WebSocketsChannelBindingModel.Method in binding)
    map.key("query", entry => parseSchema(WebSocketsChannelBindingModel.Query, binding, entry))
    map.key(
      "headers",
      entry => parseSchema(WebSocketsChannelBindingModel.Headers, binding, entry)
    )
    parseBindingVersion(binding, WebSocketsChannelBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "wsChannelBinding")

    binding
  }
}
