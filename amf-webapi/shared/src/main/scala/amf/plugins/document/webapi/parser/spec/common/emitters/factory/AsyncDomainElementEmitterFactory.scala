package amf.plugins.document.webapi.parser.spec.common.emitters.factory

import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.model.domain.DomainElement
import amf.plugins.document.webapi.contexts.emitter.async.Async20SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.async.emitters.{
  AsyncApiBindingsPartEmitter,
  AsyncApiCorrelationIdContentEmitter,
  AsyncApiMessageContentEmitter,
  AsyncApiSingleParameterPartEmitter,
  AsyncOperationPartEmitter
}
import amf.plugins.domain.webapi.models.{CorrelationId, Message, Operation, Parameter, Request, Response}
import amf.plugins.domain.webapi.models.bindings.{ChannelBindings, MessageBindings, OperationBindings, ServerBindings}

case class AsyncEmitterFactory()(implicit val ctx: Async20SpecEmitterContext) extends OasLikeEmitterFactory {

  override def parameterEmitter(p: Parameter): Option[PartEmitter] =
    Some(AsyncApiSingleParameterPartEmitter(p, SpecOrdering.Lexical))

  override def messageEmitter(m: Message): Option[PartEmitter] =
    Some(new AsyncApiMessageContentEmitter(m, isTrait = m.isAbstract.option().getOrElse(false), SpecOrdering.Lexical))

  override def operationEmitter(o: Operation): Option[PartEmitter] =
    Some(AsyncOperationPartEmitter(o, isTrait = o.isAbstract.option().getOrElse(false), SpecOrdering.Lexical))

  override def requestEmitter(r: Request): Option[PartEmitter] = messageEmitter(r)

  override def responseEmitter(r: Response): Option[PartEmitter] = messageEmitter(r)

  override def correlationIdEmitter(i: CorrelationId): Option[PartEmitter] =
    Some(AsyncApiCorrelationIdContentEmitter(i, SpecOrdering.Lexical))

  override def messageBindingsEmitter(m: MessageBindings): Option[PartEmitter] = bindingsEmitter(m)

  override def operationBindingsEmitter(o: OperationBindings): Option[PartEmitter] = bindingsEmitter(o)

  override def channelBindingsEmitter(c: ChannelBindings): Option[PartEmitter] = bindingsEmitter(c)

  override def serverBindingsEmitter(s: ServerBindings): Option[PartEmitter] = bindingsEmitter(s)

  private def bindingsEmitter(element: DomainElement): Option[PartEmitter] =
    Some(AsyncApiBindingsPartEmitter(element, SpecOrdering.Lexical, Nil))
}

object AsyncEmitterFactory {
  def apply(eh: ErrorHandler): AsyncEmitterFactory = AsyncEmitterFactory()(new Async20SpecEmitterContext(eh))

}
