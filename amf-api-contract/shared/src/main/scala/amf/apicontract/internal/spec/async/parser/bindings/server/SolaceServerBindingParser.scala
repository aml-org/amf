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
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "SolaceServerBinding", ctx.specSettings.spec)
    val map     = entry.value.as[YMap]
    val binding: SolaceServerBinding = bindingVersion match {
      case "0.1.0" | "0.2.0" | "0.3.0" | "latest" => SolaceServerBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = SolaceServerBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Solace Server Binding")
        defaultBinding
    }

    map.key("msgVpn", SolaceServerBindingModel.MsgVpn in binding)

    parseBindingVersion(binding, SolaceServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "SolaceServerBinding")

    binding
  }
}
