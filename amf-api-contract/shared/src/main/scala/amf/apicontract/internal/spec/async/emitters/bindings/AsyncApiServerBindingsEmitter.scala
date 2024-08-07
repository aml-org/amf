package amf.apicontract.internal.spec.async.emitters.bindings

import org.mulesoft.common.client.lexical.Position
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.apicontract.internal.metamodel.domain.bindings.{IBMMQServerBindingModel, KafkaServerBindingModel,MqttServerBinding020Model, MqttServerBindingModel, MqttServerLastWillModel, PulsarServerBindingModel, SolaceServerBinding040Model, SolaceServerBindingModel}
import amf.apicontract.client.scala.model.domain.bindings.ServerBinding
import amf.apicontract.client.scala.model.domain.bindings.ibmmq.IBMMQServerBinding
import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaServerBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.{MqttServerBinding, MqttServerLastWill}
import amf.apicontract.client.scala.model.domain.bindings.pulsar.PulsarServerBinding
import amf.apicontract.client.scala.model.domain.bindings.solace.SolaceServerBinding
import amf.apicontract.internal.spec.async.emitters.domain
import amf.apicontract.internal.spec.async.parser.bindings.Bindings._
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.SynthesizedField
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiServerBindingsEmitter(binding: ServerBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: ServerBinding): Option[EntryEmitter] = binding match {
    case binding: MqttServerBinding   => Some(new MqttServerBindingEmitter(binding, ordering))
    case binding: IBMMQServerBinding  => Some(new IBMMQServerBindingEmitter(binding, ordering))
    case binding: SolaceServerBinding => Some(new SolaceServerBindingEmitter(binding, ordering))
    case binding: PulsarServerBinding => Some(new PulsarServerBindingEmitter(binding, ordering))
    case binding: KafkaServerBinding  => Some(new KafkaServerBindingEmitter(binding, ordering))
    case _                            => None
  }

  override def position(): Position = pos(binding.annotations)
}

class MqttServerBindingEmitter(binding: MqttServerBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(Mqtt),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(MqttServerBindingModel.ClientId).foreach(f => result += ValueEmitter("clientId", f))
        fs.entry(MqttServerBindingModel.CleanSession).foreach(f => result += ValueEmitter("cleanSession", f))
        fs.entry(MqttServerBindingModel.KeepAlive).foreach(f => result += ValueEmitter("keepAlive", f))
        fs.entry(MqttServerBindingModel.LastWill)
          .foreach(_ => result += new LastWillEmitter(binding.lastWill, ordering))
        fs.entry(MqttServerBinding020Model.SessionExpiryInterval)
          .foreach(f => result += ValueEmitter("sessionExpiryInterval", f))
        fs.entry(MqttServerBinding020Model.SessionExpiryIntervalSchema)
          .foreach(f =>
            result += domain.AsyncSchemaEmitter("sessionExpiryInterval", f.element.asInstanceOf[Shape], ordering, Seq())
          )
        fs.entry(MqttServerBinding020Model.MaximumPacketSize)
          .foreach(f => result += ValueEmitter("maximumPacketSize", f))
        fs.entry(MqttServerBinding020Model.MaximumPacketSizeSchema)
          .foreach(f =>
            result += domain.AsyncSchemaEmitter("maximumPacketSize", f.element.asInstanceOf[Shape], ordering, Seq())
          )
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)

  class LastWillEmitter(lastWill: MqttServerLastWill, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: YDocument.EntryBuilder): Unit = {
      b.entry(
        YNode("lastWill"),
        _.obj { emitter =>
          val fs     = lastWill.fields
          val result = ListBuffer[EntryEmitter]()

          fs.entry(MqttServerLastWillModel.Topic).foreach(f => result += ValueEmitter("topic", f))
          fs.entry(MqttServerLastWillModel.Qos).foreach(f => result += ValueEmitter("qos", f))
          fs.entry(MqttServerLastWillModel.Retain).foreach(f => result += ValueEmitter("retain", f))
          fs.entry(MqttServerLastWillModel.Message).foreach(f => result += ValueEmitter("message", f))

          traverse(ordering.sorted(result), emitter)
        }
      )
    }

    override def position(): Position = pos(lastWill.annotations)
  }
}

class IBMMQServerBindingEmitter(binding: IBMMQServerBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(IBMMQ),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(IBMMQServerBindingModel.GroupId).foreach(f => result += ValueEmitter("groupId", f))
        fs.entry(IBMMQServerBindingModel.CcdtQueueManagerName)
          .foreach(f => result += ValueEmitter("ccdtQueueManagerName", f))
        fs.entry(IBMMQServerBindingModel.CipherSpec).foreach(f => result += ValueEmitter("cipherSpec", f))
        fs.entry(IBMMQServerBindingModel.MultiEndpointServer)
          .foreach(f => result += ValueEmitter("multiEndpointServer", f))
        fs.entry(IBMMQServerBindingModel.HeartBeatInterval).foreach(f => result += ValueEmitter("heartBeatInterval", f))

        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class SolaceServerBindingEmitter(binding: SolaceServerBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(Solace),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(SolaceServerBindingModel.MsgVpn).foreach(f => result += ValueEmitter("msgVpn", f))
        fs.entry(SolaceServerBinding040Model.ClientName).foreach(f => result += ValueEmitter("clientName", f))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class PulsarServerBindingEmitter(binding: PulsarServerBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(Pulsar),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(PulsarServerBindingModel.Tenant).foreach { f =>
          if (!f.value.annotations.contains(classOf[SynthesizedField])) result += ValueEmitter("tenant", f)
        }

        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class KafkaServerBindingEmitter(binding: KafkaServerBinding, ordering: SpecOrdering)
    extends AsyncApiCommonBindingEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode(Kafka),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(KafkaServerBindingModel.SchemaRegistryUrl).foreach(f => result += ValueEmitter("schemaRegistryUrl", f))
        fs.entry(KafkaServerBindingModel.SchemaRegistryVendor)
          .foreach(f => result += ValueEmitter("schemaRegistryVendor", f))

        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}
