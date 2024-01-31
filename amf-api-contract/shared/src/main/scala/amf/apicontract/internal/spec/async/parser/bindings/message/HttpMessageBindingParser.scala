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
    val binding = HttpMessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("headers", parseSchema(HttpMessageBindingModel.Headers, binding, _))
    parseBindingVersion(binding, HttpMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "httpMessageBinding")

    binding
  }
}
