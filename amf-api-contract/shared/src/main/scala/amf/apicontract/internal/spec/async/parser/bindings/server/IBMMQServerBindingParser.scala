package amf.apicontract.internal.spec.async.parser.bindings.server

import amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQServerBinding
import amf.apicontract.internal.metamodel.domain.bindings.IBMMQServerBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

object IBMMQServerBindingParser extends BindingParser[IBMMQServerBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): IBMMQServerBinding = {
    val binding = IBMMQServerBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("groupId", IBMMQServerBindingModel.GroupId in binding)

    map.key("ccdtQueueManagerName") match {
      case Some(value) => Some(value).foreach(IBMMQServerBindingModel.CcdtQueueManagerName in binding)
      case None        => setDefaultValue(binding, IBMMQServerBindingModel.CcdtQueueManagerName, AmfScalar("*"))
    }

    map.key("cipherSpec") match {
      case Some(value) => Some(value).foreach(IBMMQServerBindingModel.CipherSpec in binding)
      case None        => setDefaultValue(binding, IBMMQServerBindingModel.CipherSpec, AmfScalar("ANY"))
    }

    map.key("multiEndpointServer") match {
      case Some(value) => Some(value).foreach(IBMMQServerBindingModel.MultiEndpointServer in binding)
      case None        => setDefaultValue(binding, IBMMQServerBindingModel.MultiEndpointServer, AmfScalar(false))
    }

    map.key("heartBeatInterval") match {
      case Some(value) => Some(value).foreach(IBMMQServerBindingModel.HeartBeatInterval in binding)
      case None        => setDefaultValue(binding, IBMMQServerBindingModel.HeartBeatInterval, AmfScalar(300))
    }

    parseBindingVersion(binding, IBMMQServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "IBMMQServerBinding")

    binding
  }
}
