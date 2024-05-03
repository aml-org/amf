package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.http.{
  HttpOperationBinding,
  HttpOperationBinding010,
  HttpOperationBinding020
}
import amf.apicontract.internal.metamodel.domain.bindings.{HttpOperationBinding010Model, HttpOperationBindingModel}
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object HttpOperationBindingParser extends BindingParser[HttpOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): HttpOperationBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "HttpOperationBinding", ctx.specSettings.spec)

    val binding = bindingVersion match {
      case "0.2.0" | "0.3.0" | "latest" => HttpOperationBinding020(Annotations(entry))
      case "0.1.0"                      => HttpOperationBinding010(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = HttpOperationBinding010(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "HTTP Operation Binding", warning = true)
        defaultBinding
    }

    map.key("query", entry => parseSchema(HttpOperationBindingModel.Query, binding, entry))

    parseBindingVersion(binding, HttpOperationBindingModel.BindingVersion, map)

    binding match {
      case default: HttpOperationBinding010 =>
        map.key("type", HttpOperationBinding010Model.OperationType in binding)
        if (default.operationType.is("request")) map.key("method", HttpOperationBindingModel.Method in binding)
        ctx.closedShape(binding, map, "httpOperationBinding010")
      case _ =>
        map.key("method", HttpOperationBindingModel.Method in binding)
        ctx.closedShape(binding, map, "httpOperationBinding020")
    }

    binding
  }
}
