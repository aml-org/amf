package amf.plugins.document.webapi.parser.spec.async.emitters
import amf.core.emitter.BaseEmitters.{EmptyMapEmitter, MapEntryEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.{AmfElement, DataNode, DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
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
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

/**
  * @param bindings is an object which contain individual bindings. (ej. ServerBindings, OperationBindings, etc)
  */
abstract class AsyncApiBindingsEntryEmitter(
    bindings: AmfElement,
    ordering: SpecOrdering,
    extensions: Seq[DomainExtension])(implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  def key: String

  def emit(b: EntryBuilder): Unit = {
    val emitters: Seq[EntryEmitter] = obtainBindings(bindings)
      .flatMap(emitterForElement) ++ extensionEmitters
    b.entry(
      key,
      partBuilder =>
        if (isLink)
          emitLink(partBuilder)
        else
          partBuilder.obj { emitter =>
            traverse(ordering.sorted(emitters), emitter)
        }
    )
  }

  def isLink: Boolean = bindings match {
    case l: Linkable if l.isLink => true
    case _                       => false
  }

  def emitLink(b: PartBuilder): Unit = {
    val label = OasDefinitions.appendOas3ComponentsPrefix(bindings.asInstanceOf[Linkable].linkLabel.value(),
                                                          obtainComponentTag())
    spec.ref(b, label)
  }

  def obtainComponentTag(): String = bindings match {
    case _: ServerBindings    => "serverBindings"
    case _: OperationBindings => "operationBindings"
    case _: ChannelBindings   => "channelBindings"
    case _: MessageBindings   => "messageBindings"
    case _                    => "default"
  }

  def obtainBindings(value: AmfElement): Seq[AmfElement] = {
    value match {
      case s: ServerBindings    => s.bindings
      case s: OperationBindings => s.bindings
      case s: ChannelBindings   => s.bindings
      case s: MessageBindings   => s.bindings
      case _                    => Nil
    }
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

  def emitLink(l: Linkable): Option[EntryEmitter] = {
    val label = OasDefinitions.appendOas3ComponentsPrefix(l.linkLabel.value(), "pija")
    Some(MapEntryEmitter("$ref", label))
  }

  override def position(): Position = pos(bindings.annotations)
}

case class AsyncApiBindingsEmitter(bindings: AmfElement, ordering: SpecOrdering, extensions: Seq[DomainExtension])(
    override implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiBindingsEntryEmitter(bindings, ordering, extensions) {
  override def key: String = "bindings"
}

case class AsyncApiNamedBindingsEmitter(
    bindings: AmfElement,
    ordering: SpecOrdering,
    extensions: Seq[DomainExtension])(override implicit val spec: OasLikeSpecEmitterContext)
    extends AsyncApiBindingsEntryEmitter(bindings, ordering, extensions) {
  override def key: String = {
    val name = bindings match {
      case named: NamedDomainElement => named.name.option()
      case _                         => None
    }
    name.getOrElse("default")
  }
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

case class AsyncApiBindingsDeclarationEmitter(key: String, bindings: Seq[AmfElement], ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val namedBindingsEmitters = bindings.map(p => AsyncApiNamedBindingsEmitter(p, ordering, Nil))
    b.entry(
      key,
      _.obj(pb => namedBindingsEmitters.foreach(e => e.emit(pb)))
    )
  }

  override def position(): Position = bindings.headOption.map(b => pos(b.annotations)).getOrElse(ZERO)
}
