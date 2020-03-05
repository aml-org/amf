package amf.plugins.document.webapi.parser.spec.domain.binding

import amf.core.parser.{Annotations, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorServerBindings
import amf.plugins.domain.webapi.metamodel.bindings.{
  MqttServerBindingModel,
  MqttServerLastWillModel,
  ServerBindingModel,
  ServerBindingsModel
}
import amf.plugins.domain.webapi.models.bindings.{ServerBinding, ServerBindings}
import amf.plugins.domain.webapi.models.bindings.mqtt.{MqttServerBinding, MqttServerLastWill}
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.plugins.document.webapi.parser.spec.domain.ConversionHelpers._

object AsyncServerBindingsParser extends AsyncBindingsParser {
  override type Binding  = ServerBinding
  override type Bindings = ServerBindings

  def buildAndPopulate(entryOrMap: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): ServerBindings = {
    val map: YMap      = entryOrMap
    val serverBindings = ServerBindings(map)
    nameAndAdopt(serverBindings, entryOrMap.left.toOption, parent)
    parseBindings(serverBindings, map)
  }

  private def parseBindings(obj: ServerBindings, map: YMap)(implicit ctx: AsyncWebApiContext): ServerBindings = {
    val bindings: Seq[ServerBinding] = parseElements(map, obj.id)
    obj.withBindings(bindings)
  }

  def handleRef(entryOrNode: Either[YMapEntry, YNode], fullRef: String, parent: String)(
      implicit ctx: AsyncWebApiContext): ServerBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "serverBindings")
    ctx.declarations
      .findServerBindings(label, SearchScope.Named)
      .map(serverBindings => nameAndAdopt(serverBindings.link(label), entryOrNode.left.toOption, parent))
      .getOrElse(remote(fullRef, entryOrNode, parent))
  }

  private def remote(fullRef: String, entryOrNode: Either[YMapEntry, YNode], parent: String)(
      implicit ctx: AsyncWebApiContext): ServerBindings = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(bindingsNode) =>
        val external = AsyncServerBindingsParser.parse(Right(bindingsNode), parent)
        nameAndAdopt(external.link(fullRef), entryOrNode.left.toOption, parent)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", entryOrNode)
        nameAndAdopt(new ErrorServerBindings(fullRef, entryOrNode).link(fullRef), entryOrNode.left.toOption, parent)
    }
  }

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
        val lastWill    = MqttServerLastWill(Annotations(entry)).adopted(binding.id)
        val lastWillMap = entry.value.as[YMap]

        lastWillMap.key("topic", MqttServerLastWillModel.Topic in lastWill)
        lastWillMap.key("qos", MqttServerLastWillModel.Qos in lastWill)
        lastWillMap.key("retain", MqttServerLastWillModel.Retain in lastWill)

        ctx.closedShape(lastWill.id, lastWillMap, "mqttServerLastWill")

        binding.set(MqttServerBindingModel.LastWill, lastWill)
      }
    )
  }
}
