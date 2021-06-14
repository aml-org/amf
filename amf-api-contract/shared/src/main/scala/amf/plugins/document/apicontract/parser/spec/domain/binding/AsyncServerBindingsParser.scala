package amf.plugins.document.apicontract.parser.spec.domain.binding

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorServerBindings
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.domain.apicontract.metamodel.bindings.{
  MqttServerBindingModel,
  MqttServerLastWillModel,
  ServerBindingsModel
}
import amf.plugins.domain.apicontract.models.bindings.mqtt.{MqttServerBinding, MqttServerLastWill}
import amf.plugins.domain.apicontract.models.bindings.{ServerBinding, ServerBindings}
import org.yaml.model.{YMap, YMapEntry}

import scala.Console.in

case class AsyncServerBindingsParser(entryLike: YMapEntryLike, parent: String)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike, parent) {

  override type Binding  = ServerBinding
  override type Bindings = ServerBindings
  override val bindingsField: Field = ServerBindingsModel.Bindings

  override protected def createParser(entryLike: YMapEntryLike): AsyncBindingsParser =
    AsyncServerBindingsParser(entryLike, parent)

  override protected def createBindings(): ServerBindings = ServerBindings()

  def handleRef(fullRef: String): ServerBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "serverBindings")
    ctx.declarations
      .findServerBindings(label, SearchScope.Named)
      .map(serverBindings =>
        nameAndAdopt(serverBindings.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
                     entryLike.key))
      .getOrElse(remote(fullRef, entryLike, parent))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): ServerBindings =
    new ErrorServerBindings(fullRef, entryLike.asMap)

  override protected def parseMqtt(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): ServerBinding = {
    val binding = MqttServerBinding(Annotations(entry)).adopted(parent)
    val map     = entry.value.as[YMap]

    map.key("clientId", MqttServerBindingModel.ClientId in binding)
    map.key("cleanSession", MqttServerBindingModel.CleanSession in binding)
    map.key("keepAlive", MqttServerBindingModel.KeepAlive in binding)

    parseLastWill(binding, map)

    parseBindingVersion(binding, MqttServerBindingModel.BindingVersion, map)

    ctx.closedShape(binding.id, map, "mqttServerBinding")

    binding
  }

  private def parseLastWill(binding: MqttServerBinding, map: YMap)(implicit ctx: AsyncWebApiContext): Unit = {
    map.key(
      "lastWill", { entry =>
        val lastWill    = MqttServerLastWill(Annotations(entry.value)).adopted(binding.id)
        val lastWillMap = entry.value.as[YMap]

        lastWillMap.key("topic", MqttServerLastWillModel.Topic in lastWill)
        lastWillMap.key("qos", MqttServerLastWillModel.Qos in lastWill)
        lastWillMap.key("retain", MqttServerLastWillModel.Retain in lastWill)
        lastWillMap.key("message", MqttServerLastWillModel.Message in lastWill)

        ctx.closedShape(lastWill.id, lastWillMap, "mqttServerLastWill")
        binding.set(MqttServerBindingModel.LastWill, lastWill, Annotations(entry))
      }
    )
  }
}
