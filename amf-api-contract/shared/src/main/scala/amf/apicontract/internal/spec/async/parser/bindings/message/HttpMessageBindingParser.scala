package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.http.{
  HttpMessageBinding,
  HttpMessageBinding020,
  HttpMessageBinding030
}
import amf.apicontract.internal.metamodel.domain.bindings.{HttpMessageBinding030Model, HttpMessageBindingModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object HttpMessageBindingParser extends BindingParser[HttpMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): HttpMessageBinding = {
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "HttpMessageBinding", ctx.specSettings.spec)
    val map            = entry.value.as[YMap]
    val binding = bindingVersion match {
      case "0.3.0" | "latest" => HttpMessageBinding030(Annotations(entry))
      case "0.1.0" | "0.2.0"  => HttpMessageBinding020(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = HttpMessageBinding020(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "HTTP Message Binding", warning = true)
        defaultBinding
    }

    map.key("headers", parseSchema(HttpMessageBindingModel.Headers, binding, _))
    parseBindingVersion(binding, HttpMessageBindingModel.BindingVersion, map)

    bindingVersion match {
      case "0.3.0" | "latest" =>
        map.key("statusCode", HttpMessageBinding030Model.StatusCode in binding)
        ctx.closedShape(binding, map, "httpMessageBinding030")
      case _ =>
        ctx.closedShape(binding, map, "httpMessageBinding020")
    }

    binding
  }
}
