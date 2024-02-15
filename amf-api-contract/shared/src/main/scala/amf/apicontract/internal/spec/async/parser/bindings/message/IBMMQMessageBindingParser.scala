package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQMessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object IBMMQMessageBindingParser extends BindingParser[IBMMQMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): IBMMQMessageBinding = {
    val binding = IBMMQMessageBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("type") match {
      case Some(value) => Some(value).foreach(IBMMQMessageBindingModel.MessageType in binding)
      case None        => setDefaultValue(binding, IBMMQMessageBindingModel.MessageType, AmfScalar("string"))
    }

    map.key("headers").foreach { entry =>
      val values = entry.value.toString.split(",").map(AmfScalar(_)).toSeq
      binding.setWithoutId(
        IBMMQMessageBindingModel.Headers,
        AmfArray(values, Annotations.virtual()),
        Annotations(entry)
      )
    }

    map.key("description", IBMMQMessageBindingModel.Description in binding)

    map.key("expiry") match {
      case Some(value) => Some(value).foreach(IBMMQMessageBindingModel.Expiry in binding)
      case None        => setDefaultValue(binding, IBMMQMessageBindingModel.Expiry, AmfScalar(0))
    }

    parseBindingVersion(binding, IBMMQMessageBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "IBMMQMessageBinding")

    binding
  }
}
