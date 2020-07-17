package amf.plugins.document.webapi.parser.spec.common.emitters.factory

import amf.core.annotations.DeclaredElement
import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.annotations.{FormBodyParameter, RequiredParamPayload}
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterContext,
  Oas3SpecEmitterContext,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypePartEmitter
import amf.plugins.document.webapi.parser.spec.domain.{
  Oas3ExampleValuesPartEmitter,
  OasCallbackEmitter,
  OasLinkPartEmitter,
  OasResponsePartEmitter,
  ParameterEmitter,
  PayloadAsParameterEmitter
}
import amf.plugins.document.webapi.parser.spec.oas.Oas3RequestBodyPartEmitter
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models.{Callback, Parameter, Payload, Request, Response, TemplatedLink}

object Oas20EmitterFactory extends OasEmitterFactory {
  // TODO ajust error handler
  implicit val ctx: Oas2SpecEmitterContext = new Oas2SpecEmitterContext(UnhandledErrorHandler, compactEmission = false)

  override def payloadEmitter(p: Payload): Option[PartEmitter] = {
    if (isParamPayload(p))
      Some(PayloadAsParameterEmitter(p, SpecOrdering.Lexical, Nil))
    else None
  }

  private def isParamPayload(p: Payload) = {
    p.annotations
      .find(a =>
        a match {
          case _: RequiredParamPayload | FormBodyParameter() | DeclaredElement() => true
          case _                                                                 => false
      })
      .isDefined
  }
}

object Oas30EmitterFactory extends OasEmitterFactory {
  // TODO ajust error handler
  implicit val ctx: Oas3SpecEmitterContext = new Oas3SpecEmitterContext(UnhandledErrorHandler, compactEmission = false)

  override def exampleEmitter(example: Example): Option[PartEmitter] =
    Some(Oas3ExampleValuesPartEmitter(example, SpecOrdering.Lexical))

  override def templatedLinkEmitter(link: TemplatedLink): Option[PartEmitter] =
    Some(OasLinkPartEmitter(link, SpecOrdering.Lexical, Nil))

  override def callbackEmitter(callback: Callback): Option[PartEmitter] =
    Some(OasCallbackEmitter(List(callback), SpecOrdering.Lexical, Nil))

  override def requestEmitter(r: Request): Option[PartEmitter] =
    Some(Oas3RequestBodyPartEmitter(r, SpecOrdering.Lexical, Nil))
}

trait OasEmitterFactory extends OasLikeEmitterFactory {

  implicit val ctx: OasSpecEmitterContext

  override def parameterEmitter(p: Parameter): Option[PartEmitter] =
    // TODO verify if parameter is an explicit header to adjust boolean of emitter
    Some(ParameterEmitter(p, SpecOrdering.Lexical, Nil, asHeader = false))

  override def responseEmitter(r: Response): Option[PartEmitter] =
    Some(OasResponsePartEmitter(r, SpecOrdering.Lexical, Nil))
}

trait OasLikeEmitterFactory extends DomainElementEmitterFactory {

  implicit val ctx: OasLikeSpecEmitterContext

  override def typeEmitter(s: Shape): Option[PartEmitter] = {
    Some(OasTypePartEmitter(s, SpecOrdering.Lexical, references = Nil))
  }

}
