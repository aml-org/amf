package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.solace.SolaceServerBinding
import amf.apicontract.internal.metamodel.domain.bindings.SolaceServerBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object SolaceServerBindingParser extends BindingParser[SolaceServerBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): SolaceServerBinding = {
    val binding = SolaceServerBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("msgVpn", SolaceServerBindingModel.MsgVpn in binding)

    parseBindingVersion(binding, SolaceServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "SolaceServerBinding")

    binding
  }
}
