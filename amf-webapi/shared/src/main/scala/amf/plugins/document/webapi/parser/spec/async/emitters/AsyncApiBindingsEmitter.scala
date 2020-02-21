package amf.plugins.document.webapi.parser.spec.async.emitters
import amf.core.emitter.BaseEmitters.{EmptyMapEmitter, ScalarEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.{AmfElement, AmfScalar, Annotation, DataNode}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.async.emitters.bindings.{
  AsyncApiChannelBindingsEmitter,
  AsyncApiMessageBindingsEmitter,
  AsyncApiOperationBindingsEmitter,
  AsyncApiServerBindingsEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.{
  AnnotationsEmitter,
  DataNodeEmitter,
  OrphanAnnotationsEmitter
}
import amf.plugins.domain.webapi.metamodel.bindings.DynamicBindingModel
import amf.plugins.domain.webapi.models.bindings._
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.YNode

import scala.collection.mutable.ListBuffer

class AsyncApiBindingsEmitter(fieldEntry: FieldEntry, ordering: SpecOrdering, extensions: Seq[DomainExtension])(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  def emit(b: EntryBuilder): Unit = {
    val emitters: Seq[EntryEmitter] = fieldEntry
      .arrayValues[AmfElement]
      .flatMap(emitterForElement) ++ extensionEmitters
    b.entry(
      "bindings",
      _.obj { emitter =>
        traverse(ordering.sorted(emitters), emitter)
      }
    )
  }

  def extensionEmitters: Seq[EntryEmitter] = OrphanAnnotationsEmitter(extensions, ordering).emitters

  def emitterForElement(element: AmfElement): Option[EntryEmitter] = {
    element match {
      case binding: EmptyBinding     => Some(new EmptyBindingEmitter(binding, ordering))
      case binding: DynamicBinding   => Some(new DynamicBindingEmitter(binding, ordering))
      case binding: ChannelBinding   => Some(new AsyncApiChannelBindingsEmitter(binding, ordering))
      case binding: ServerBinding    => Some(new AsyncApiServerBindingsEmitter(binding, ordering))
      case binding: OperationBinding => Some(new AsyncApiOperationBindingsEmitter(binding, ordering))
      case binding: MessageBinding   => Some(new AsyncApiMessageBindingsEmitter(binding, ordering))
      case _                         => None
    }
  }

  override def position(): Position = pos(fieldEntry.value.annotations)
}

class DynamicBindingEmitter(binding: DynamicBinding, ordering: SpecOrdering)(implicit val spec: SpecEmitterContext)
    extends EntryEmitter {
  def emit(b: EntryBuilder): Unit = {
    val bindingType = binding.`type`.value()
    val fs          = binding.fields
    val result      = ListBuffer[EntryEmitter]()

    fs.entry(DynamicBindingModel.Definition)
      .foreach(f => result ++= DataNodeEmitter(f.element.asInstanceOf[DataNode], ordering)(spec.eh).emitters())
    b.entry(
      YNode(bindingType),
      _.obj(traverse(ordering.sorted(result), _))
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class EmptyBindingEmitter(binding: EmptyBinding, ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val bindingType = binding.`type`.value()
    b.entry(
      YNode(bindingType),
      emitter => EmptyMapEmitter().emit(emitter)
    )
  }

  override def position(): Position = pos(binding.annotations)
}
