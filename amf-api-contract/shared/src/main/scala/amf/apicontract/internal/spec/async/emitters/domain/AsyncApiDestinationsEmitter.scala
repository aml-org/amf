package amf.apicontract.internal.spec.async.emitters.domain

import amf.apicontract.client.scala.model.domain.bindings.solace.{
  SolaceOperationDestination,
  SolaceOperationDestination010,
  SolaceOperationDestination020,
  SolaceOperationDestination030,
  SolaceOperationDestination040,
  SolaceOperationQueue,
  SolaceOperationQueue030,
  SolaceOperationTopic
}
import amf.apicontract.internal.metamodel.domain.bindings.{
  SolaceOperationDestinationModel,
  SolaceOperationQueue010Model,
  SolaceOperationQueue030Model,
  SolaceOperationQueueModel,
  SolaceOperationTopicModel
}
import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{ArrayEmitter, RawEmitter, ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

case class AsyncApiDestinationsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  val key = "destinations"

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val destinations =
      f.array.values
        .map(_.asInstanceOf[SolaceOperationDestination])
        .map(new SingleDestinationEmitter(_, ordering))

    b.entry(
      key,
      _.list(traverse(destinations, _))
    )
  }

  override def position(): Position = pos(f.element.annotations)
}

class SingleDestinationEmitter(destination: SolaceOperationDestination, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends PartEmitter {
  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)
  override def emit(b: YDocument.PartBuilder): Unit = {

    val result = ListBuffer[EntryEmitter]()
    val fs     = destination.fields

    fs.entry(SolaceOperationDestinationModel.DestinationType).foreach(f => result += ValueEmitter("destinationType", f))
    fs.entry(SolaceOperationDestinationModel.DeliveryMode).foreach { f =>
      // in parsing the field may have been added as default value and synthesized, if so we should not emit it
      if (!f.value.annotations.isSynthesized) {
        result += ValueEmitter("deliveryMode", f)
      }
    }

    Option(destination.queue).foreach { queue =>
      val emitter = new SolaceOperationQueueEmitter(queue, ordering)
      result += emitter
    }
    destination match {
      case binding020: SolaceOperationDestination020 =>
        Option(binding020.topic).foreach(topic => result += new SolaceOperationTopicEmitter(topic, ordering))
      case binding030: SolaceOperationDestination030 =>
        Option(binding030.topic).foreach(topic => result += new SolaceOperationTopicEmitter(topic, ordering))
      case binding040: SolaceOperationDestination040 =>
        Option(binding040.topic).foreach(topic => result += new SolaceOperationTopicEmitter(topic, ordering))
      case _ =>
    }

    result ++= AnnotationsEmitter(destination, ordering).emitters

    b.obj(traverse(ordering.sorted(result), _))
  }

  override def position(): Position = pos(destination.annotations)
}

class SolaceOperationQueueEmitter(queue: SolaceOperationQueue, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("queue"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = queue.fields

        fs.entry(SolaceOperationQueueModel.Name).foreach(f => result += ValueEmitter("name", f))
        fs.entry(SolaceOperationQueueModel.TopicSubscriptions)
          .foreach(f => result += spec.arrayEmitter("topicSubscriptions", f, ordering))
        fs.entry(SolaceOperationQueueModel.AccessType).foreach(f => result += ValueEmitter("accessType", f))
        fs.entry(SolaceOperationQueue030Model.MaxMsgSpoolSize)
          .foreach(f => result += ValueEmitter("maxMsgSpoolSize", f))
        fs.entry(SolaceOperationQueue030Model.MaxTtl).foreach(f => result += ValueEmitter("maxTtl", f))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(queue.annotations)
}

class SolaceOperationTopicEmitter(topic: SolaceOperationTopic, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("topic"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = topic.fields

        fs.entry(SolaceOperationQueueModel.TopicSubscriptions)
          .foreach(f => result += spec.arrayEmitter("topicSubscriptions", f, ordering))

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(topic.annotations)
}
