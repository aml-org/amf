package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQServerBinding
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQServerBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object IBMMQServerBindingParser extends BindingParser[IBMMQServerBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): IBMMQServerBinding = {
    val binding = IBMMQServerBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("groupId", IBMMQServerBindingModel.GroupId in binding)
    map.key("ccdtQueueManagerName", IBMMQServerBindingModel.CcdtQueueManagerName in binding)
    map.key("cipherSpec", IBMMQServerBindingModel.CipherSpec in binding)
    map.key("multiEndpointServer", IBMMQServerBindingModel.MultiEndpointServer in binding)
    map.key("heartBeatInterval", IBMMQServerBindingModel.HeartBeatInterval in binding)

    parseBindingVersion(binding, IBMMQServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "IBMMQServerBinding")

    binding
  }
}
