package amf.plugins.document.webapi.parser.spec.common.emitters.factory

import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.emitter.raml.{
  Raml08SpecEmitterContext,
  Raml10SpecEmitterContext,
  RamlSpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.{AbstractDeclarationPartEmitter, DataNodeEmitter}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{Raml08TypePartEmitter, Raml10TypePartEmitter}
import amf.plugins.document.webapi.parser.spec.domain.{Raml08ResponsePartEmitter, Raml10ResponsePartEmitter}
import amf.plugins.domain.webapi.models.Response
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}

case class Raml10EmitterFactory()(implicit val ctx: Raml10SpecEmitterContext) extends RamlEmitterFactory {

  override def typeEmitter(s: Shape): Option[PartEmitter] =
    Some(Raml10TypePartEmitter(s, SpecOrdering.Lexical, None, references = Nil))

  override def responseEmitter(e: Response): Option[PartEmitter] =
    Some(Raml10ResponsePartEmitter(e, SpecOrdering.Lexical, Nil))
}

object Raml10EmitterFactory {
  def apply(eh: ErrorHandler): Raml10EmitterFactory = Raml10EmitterFactory()(new Raml10SpecEmitterContext(eh))
}

case class Raml08EmitterFactory()(implicit val ctx: Raml08SpecEmitterContext) extends RamlEmitterFactory {

  override def typeEmitter(s: Shape): Option[PartEmitter] =
    Some(Raml08TypePartEmitter(s, SpecOrdering.Lexical, None, references = Nil))

  override def responseEmitter(e: Response): Option[PartEmitter] =
    Some(Raml08ResponsePartEmitter(e, SpecOrdering.Lexical, Nil))
}

object Raml08EmitterFactory {
  def apply(eh: ErrorHandler): Raml08EmitterFactory = Raml08EmitterFactory()(new Raml08SpecEmitterContext(eh))
}

trait RamlEmitterFactory extends DomainElementEmitterFactory {

  implicit val ctx: RamlSpecEmitterContext

  override def traitEmitter(t: Trait): Option[PartEmitter] = Some(abstractDeclarationEmitter(t))

  override def resourceTypeEmitter(r: ResourceType): Option[PartEmitter] = Some(abstractDeclarationEmitter(r))

  private def abstractDeclarationEmitter(a: AbstractDeclaration): PartEmitter =
    AbstractDeclarationPartEmitter(a, SpecOrdering.Lexical, Nil)
}
