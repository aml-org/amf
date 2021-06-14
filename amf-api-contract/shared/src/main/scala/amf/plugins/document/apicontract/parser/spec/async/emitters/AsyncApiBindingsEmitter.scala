package amf.plugins.document.apicontract.parser.spec.async.emitters
import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{AmfElement, DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.render.BaseEmitters.{EmptyMapEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.plugins.document.apicontract.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.async.emitters.bindings.{
  AsyncApiChannelBindingsEmitter,
  AsyncApiMessageBindingsEmitter,
  AsyncApiOperationBindingsEmitter,
  AsyncApiServerBindingsEmitter
}
import amf.plugins.document.apicontract.parser.spec.declaration.OasTagToReferenceEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.AgnosticShapeEmitterContextAdapter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.OrphanAnnotationsEmitter
import amf.plugins.domain.apicontract.models.bindings._
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YNode

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
    b.entry(
      key,
      AsyncApiBindingsPartEmitter(bindings, ordering, extensions).emit(_)
    )
  }

  override def position(): Position = pos(bindings.annotations)
}

case class AsyncApiBindingsPartEmitter(bindings: AmfElement, ordering: SpecOrdering, extensions: Seq[DomainExtension])(
    implicit val spec: OasLikeSpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)

  def emit(b: PartBuilder): Unit = {
    val emitters: Seq[EntryEmitter] = obtainBindings(bindings)
      .flatMap(emitterForElement) ++ extensionEmitters
    if (isLink) emitLink(b)
    else
      b.obj { emitter =>
        traverse(ordering.sorted(emitters), emitter)
      }
  }

  def isLink: Boolean = bindings match {
    case l: Linkable if l.isLink => true
    case _                       => false
  }

  def emitLink(b: PartBuilder): Unit = {
    val linkable = bindings.asInstanceOf[DomainElement with Linkable]
    OasTagToReferenceEmitter(linkable).emit(b)
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
      case binding: ChannelBinding   => Some(new AsyncApiChannelBindingsEmitter(binding, ordering))
      case binding: ServerBinding    => Some(new AsyncApiServerBindingsEmitter(binding, ordering))
      case binding: OperationBinding => Some(new AsyncApiOperationBindingsEmitter(binding, ordering))
      case binding: MessageBinding   => Some(new AsyncApiMessageBindingsEmitter(binding, ordering))
      case _                         => None
    }
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
