package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.pulsar.PulsarServerBinding
import amf.apicontract.internal.metamodel.domain.bindings.PulsarServerBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object PulsarServerBindingParser extends BindingParser[PulsarServerBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): PulsarServerBinding = {
    val binding = PulsarServerBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("tenant") match {
      case Some(value) => Some(value).foreach(PulsarServerBindingModel.Tenant in binding)
      case None        => setDefaultValue(binding, PulsarServerBindingModel.Tenant, AmfScalar("public"))
    }

    parseBindingVersion(binding, PulsarServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "PulsarServerBinding")

    binding
  }
}
