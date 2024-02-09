package amf.apicontract.internal.spec.async.parser.bindings.operation

import amf.apicontract.client.scala.model.domain.bindings.http.HttpOperationBinding
import amf.apicontract.internal.metamodel.domain.bindings.HttpOperationBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object HttpOperationBindingParser extends BindingParser[HttpOperationBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): HttpOperationBinding = {
    val binding = HttpOperationBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("type", HttpOperationBindingModel.OperationType in binding)
    if (binding.operationType.is("request")) map.key("method", HttpOperationBindingModel.Method in binding)
    map.key("query", entry => parseSchema(HttpOperationBindingModel.Query, binding, entry))
    parseBindingVersion(binding, HttpOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "httpOperationBinding")

    binding
  }
}
