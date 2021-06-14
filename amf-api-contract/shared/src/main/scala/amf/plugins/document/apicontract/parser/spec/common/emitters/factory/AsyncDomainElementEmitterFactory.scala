package amf.plugins.document.apicontract.parser.spec.common.emitters.factory

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.DomainElement
import amf.core.emitter.SpecOrdering
import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.core.model.domain.DomainElement
import amf.plugins.document.apicontract.contexts.emitter.async.Async20SpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.async.emitters.{
  AsyncApiBindingsPartEmitter,
  AsyncApiCorrelationIdContentEmitter,
  AsyncApiMessageContentEmitter,
  AsyncApiServerPartEmitter,
  AsyncApiSingleEndpointEmitter,
  AsyncApiSingleParameterPartEmitter,
  AsyncOperationPartEmitter,
  AsyncSingleSchemePartEmitter
}
import amf.plugins.document.apicontract.parser.spec.domain.ExampleDataNodePartEmitter
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.apicontract.models.{
  CorrelationId,
  EndPoint,
  Message,
  Operation,
  Parameter,
  Request,
  Response,
  Server
}
import amf.plugins.domain.apicontract.models.bindings.{
  ChannelBindings,
  MessageBindings,
  OperationBindings,
  ServerBindings
}
import amf.plugins.domain.apicontract.models.security.SecurityScheme

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

  override def serverEmitter(s: Server): Option[PartEmitter] =
    Some(new AsyncApiServerPartEmitter(s, SpecOrdering.Lexical))

  override def securitySchemeEmitter(s: SecurityScheme): Option[PartEmitter] =
    Some(AsyncSingleSchemePartEmitter(s, SpecOrdering.Lexical))

  override def exampleEmitter(example: Example): Option[PartEmitter] =
    Some(ExampleDataNodePartEmitter(example, SpecOrdering.Lexical))

  override def endpointEmitter(e: EndPoint): Option[PartEmitter] =
    Some(new AsyncApiSingleEndpointEmitter(e, SpecOrdering.Lexical))
}

object AsyncEmitterFactory {
  def apply(eh: AMFErrorHandler): AsyncEmitterFactory = AsyncEmitterFactory()(new Async20SpecEmitterContext(eh))

}
