package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQMessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object AnypointMQMessageBindingParser extends BindingParser[AnypointMQMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): AnypointMQMessageBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "AnypointMQMessageBinding", ctx.specSettings.spec)

    val binding: AnypointMQMessageBinding = bindingVersion match {
      case "0.1.0" | "latest" => AnypointMQMessageBinding(Annotations(entry))
      case "0.0.1"            => AnypointMQMessageBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = AnypointMQMessageBinding(Annotations(entry))
        invalidBindingVersion(defaultBinding, invalidVersion, "AnypointMQ Binding")
        defaultBinding
    }

    map.key("headers").foreach { entry =>
      ctx.link(entry.value) match {
        case Left(fullRef) => handleRef(fullRef, "schemas", entry, AnypointMQMessageBindingModel.Headers, binding)
        case Right(_)      => parseSchema(AnypointMQMessageBindingModel.Headers, binding, entry)
      }
    }

    parseBindingVersion(binding, AnypointMQMessageBindingModel.BindingVersion, map)
    ctx.closedShape(binding, map, "AnypointMQMessageBinding")
    binding
  }
}
