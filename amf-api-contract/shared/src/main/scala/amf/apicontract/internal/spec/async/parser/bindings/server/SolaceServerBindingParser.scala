package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.solace.{SolaceServerBinding, SolaceServerBinding010, SolaceServerBinding040}
import amf.apicontract.internal.metamodel.domain.bindings.{SolaceServerBinding040Model, SolaceServerBindingModel}
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
      case "0.4.0" | "latest" => SolaceServerBinding040(Annotations(entry))
      case "0.1.0" | "0.2.0" | "0.3.0" => SolaceServerBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = SolaceServerBinding010(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "Solace Server Binding")
        defaultBinding
    }

    map.key("msgVpn", SolaceServerBindingModel.MsgVpn in binding)
    bindingVersion match {
      case "0.4.0" | "latest" =>
        map.key("clientName", SolaceServerBinding040Model.ClientName in binding.asInstanceOf[SolaceServerBinding040])
      case _ =>
    }

    parseBindingVersion(binding, SolaceServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "SolaceServerBinding")

    binding
  }
}
