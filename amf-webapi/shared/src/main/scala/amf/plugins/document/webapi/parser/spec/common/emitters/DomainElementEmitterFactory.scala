package amf.plugins.document.webapi.parser.spec.common.emitters

import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.domain.Shape
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.async.Async20SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterContext,
  Oas3SpecEmitterContext,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.contexts.emitter.raml.{Raml08SpecEmitterContext, Raml10SpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypePartEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{Raml08TypePartEmitter, Raml10TypePartEmitter}
import amf.plugins.document.webapi.parser.spec.domain.{Raml08ResponsePartEmitter, Raml10ResponsePartEmitter}
import amf.plugins.domain.webapi.models.Response

trait DomainElementEmitterFactory {
  def typeEmitter(s: Shape): Option[PartEmitter]
  def responseEmitter(e: Response): Option[PartEmitter]
}
object DomainElementEmitterFactory {
  def apply(vendor: Vendor): DomainElementEmitterFactory = vendor match {
    case Vendor.RAML08  => Raml08EmitterFactory
    case Vendor.RAML10  => Raml10EmitterFactory
    case Vendor.OAS20   => Oas20EmitterFactory
    case Vendor.OAS30   => Oas30EmitterFactory
    case Vendor.ASYNC20 => AsyncEmitterFactory
    case _              => ??? // TODO handle
  }
}

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

object Oas20EmitterFactory extends OasLikeEmitterFactory {
  // TODO ajust error handler
  implicit val ctx: Oas2SpecEmitterContext = new Oas2SpecEmitterContext(UnhandledErrorHandler, compactEmission = false)

  override def responseEmitter(e: Response): Option[PartEmitter] = ???
}

object Oas30EmitterFactory extends OasLikeEmitterFactory {
  // TODO ajust error handler
  implicit val ctx: Oas3SpecEmitterContext = new Oas3SpecEmitterContext(UnhandledErrorHandler, compactEmission = false)

  override def responseEmitter(e: Response): Option[PartEmitter] = ???
}

object AsyncEmitterFactory extends OasLikeEmitterFactory {
  // TODO ajust error handler
  implicit val ctx: Async20SpecEmitterContext = new Async20SpecEmitterContext(UnhandledErrorHandler)

  override def responseEmitter(e: Response): Option[PartEmitter] = ???
}

trait OasLikeEmitterFactory extends DomainElementEmitterFactory {

  implicit val ctx: OasLikeSpecEmitterContext

  def typeEmitter(s: Shape): Option[PartEmitter] = {
    Some(OasTypePartEmitter(s, SpecOrdering.Lexical, references = Nil))
  }

}
