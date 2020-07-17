package amf.plugins.document.webapi.parser.spec.common.emitters.factory

import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.emitter.raml.{Raml08SpecEmitterContext, Raml10SpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{Raml08TypePartEmitter, Raml10TypePartEmitter}
import amf.plugins.document.webapi.parser.spec.domain.{Raml08ResponsePartEmitter, Raml10ResponsePartEmitter}
import amf.plugins.domain.webapi.models.Response

object Raml10EmitterFactory extends DomainElementEmitterFactory {
  // TODO ajust error handler
  implicit val ctx: Raml10SpecEmitterContext = new Raml10SpecEmitterContext(UnhandledErrorHandler)

  override def typeEmitter(s: Shape): Option[PartEmitter] =
    Some(Raml10TypePartEmitter(s, SpecOrdering.Lexical, None, references = Nil))

  override def responseEmitter(e: Response): Option[PartEmitter] =
    Some(Raml10ResponsePartEmitter(e, SpecOrdering.Lexical, Nil))
}

object Raml08EmitterFactory extends DomainElementEmitterFactory {
  // TODO ajust error handler
  implicit val ctx: Raml08SpecEmitterContext = new Raml08SpecEmitterContext(UnhandledErrorHandler)

  override def typeEmitter(s: Shape): Option[PartEmitter] =
    Some(Raml08TypePartEmitter(s, SpecOrdering.Lexical, None, references = Nil))

  override def responseEmitter(e: Response): Option[PartEmitter] =
    Some(Raml08ResponsePartEmitter(e, SpecOrdering.Lexical, Nil))
}
