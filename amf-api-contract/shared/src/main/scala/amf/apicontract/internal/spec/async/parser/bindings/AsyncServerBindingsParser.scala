package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.mqtt.{MqttServerBinding, MqttServerLastWill}
import amf.apicontract.client.scala.model.domain.bindings.{ServerBinding, ServerBindings}
import amf.apicontract.internal.metamodel.domain.bindings.{
  MqttServerBindingModel,
  MqttServerLastWillModel,
  ServerBindingsModel
}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorServerBindings
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry}

case class AsyncServerBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike) {

  override type Binding  = ServerBinding
  override type Bindings = ServerBindings
  override val bindingsField: Field = ServerBindingsModel.Bindings

  override protected def createParser(entryLike: YMapEntryLike): AsyncBindingsParser =
    AsyncServerBindingsParser(entryLike)

  override protected def createBindings(): ServerBindings = ServerBindings()

  def handleRef(fullRef: String): ServerBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "serverBindings")
    ctx.declarations
      .findServerBindings(label, SearchScope.Named)
      .map(serverBindings =>
        nameAndAdopt(serverBindings.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
                     entryLike.key))
      .getOrElse(remote(fullRef, entryLike))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): ServerBindings =
    new ErrorServerBindings(fullRef, entryLike.asMap)

  override protected def parseMqtt(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): ServerBinding = {
    val binding = MqttServerBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("clientId", MqttServerBindingModel.ClientId in binding)
    map.key("cleanSession", MqttServerBindingModel.CleanSession in binding)
    map.key("keepAlive", MqttServerBindingModel.KeepAlive in binding)

    parseLastWill(binding, map)

    parseBindingVersion(binding, MqttServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "mqttServerBinding")

    binding
  }

  private def parseLastWill(binding: MqttServerBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "lastWill", { entry =>
        val lastWill    = MqttServerLastWill(Annotations(entry.value))
        val lastWillMap = entry.value.as[YMap]

        lastWillMap.key("topic", MqttServerLastWillModel.Topic in lastWill)
        lastWillMap.key("qos", MqttServerLastWillModel.Qos in lastWill)
        lastWillMap.key("retain", MqttServerLastWillModel.Retain in lastWill)
        lastWillMap.key("message", MqttServerLastWillModel.Message in lastWill)

        ctx.closedShape(lastWill, lastWillMap, "mqttServerLastWill")
        binding.setWithoutId(MqttServerBindingModel.LastWill, lastWill, Annotations(entry))
      }
    )
  }
}
