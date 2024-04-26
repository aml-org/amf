package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.http.HttpMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.{HttpMessageBindingModel, KafkaMessageBindingModel}
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
      case "0.1.0" | "0.2.0" | "latest" => HttpMessageBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = HttpMessageBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "HTTP Message Binding", warning = true)
        defaultBinding
    }

    map.key("headers", parseSchema(HttpMessageBindingModel.Headers, binding, _))
    parseBindingVersion(binding, HttpMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "httpMessageBinding")

    binding
  }
}
