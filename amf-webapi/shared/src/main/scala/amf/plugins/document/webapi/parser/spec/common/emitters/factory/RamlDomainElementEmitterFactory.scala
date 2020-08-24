package amf.plugins.document.webapi.parser.spec.common.emitters.factory

import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.Shape
import amf.core.model.domain.templates.AbstractDeclaration
import amf.plugins.document.webapi.contexts.emitter.raml.{
  Raml08SpecEmitterContext,
  Raml10SpecEmitterContext,
  RamlSpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.common.emitters.EntryToPartEmitterAdapter
import amf.plugins.document.webapi.parser.spec.declaration.{AbstractDeclarationPartEmitter, RamlCreativeWorkEmitter}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{Raml08TypePartEmitter, Raml10TypePartEmitter}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.raml.emitters.{Raml08SecuritySchemeEmitter, Raml10SecuritySchemeEmitter}
import amf.plugins.domain.shapes.models.{CreativeWork, Example}
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter, Payload, Response}

case class Raml10EmitterFactory()(implicit val ctx: Raml10SpecEmitterContext) extends RamlEmitterFactory {

  override def typeEmitter(s: Shape): Option[PartEmitter] =
    Some(Raml10TypePartEmitter(s, SpecOrdering.Lexical, None, references = Nil))

  override def responseEmitter(e: Response): Option[PartEmitter] =
    Some(Raml10ResponsePartEmitter(e, SpecOrdering.Lexical, Nil))

  override def securitySchemeEmitter(s: SecurityScheme): Option[PartEmitter] =
    Some(Raml10SecuritySchemeEmitter(s, Nil, SpecOrdering.Lexical))

  override def parameterEmitter(p: Parameter): Option[PartEmitter] =
    Some(Raml10ParameterPartEmitter(p, SpecOrdering.Lexical, Nil))

  override def operationEmitter(o: Operation): Option[PartEmitter] =
    Some(Raml10OperationPartEmitter(o, SpecOrdering.Lexical, Nil))

  override def payloadEmitter(p: Payload): Option[PartEmitter] =
    Some(EntryToPartEmitterAdapter(Raml10PayloadEmitter(p, SpecOrdering.Lexical, Nil)))

  override def endpointEmitter(e: EndPoint): Option[PartEmitter] = Some(Raml10EndPointEmitter(e, SpecOrdering.Lexical))
}

object Raml10EmitterFactory {
  def apply(eh: ErrorHandler): Raml10EmitterFactory = Raml10EmitterFactory()(new Raml10SpecEmitterContext(eh))
}

case class Raml08EmitterFactory()(implicit val ctx: Raml08SpecEmitterContext) extends RamlEmitterFactory {

  override def typeEmitter(s: Shape): Option[PartEmitter] =
    Some(Raml08TypePartEmitter(s, SpecOrdering.Lexical, None, references = Nil))

  override def responseEmitter(e: Response): Option[PartEmitter] =
    Some(Raml08ResponsePartEmitter(e, SpecOrdering.Lexical, Nil))

  override def securitySchemeEmitter(s: SecurityScheme): Option[PartEmitter] =
    Some(Raml08SecuritySchemeEmitter(s, Nil, SpecOrdering.Lexical))

  override def parameterEmitter(p: Parameter): Option[PartEmitter] =
    Some(Raml08ParameterPartEmitter(p, SpecOrdering.Lexical, Nil))

  override def operationEmitter(o: Operation): Option[PartEmitter] =
    Some(Raml08OperationPartEmitter(o, SpecOrdering.Lexical, Nil))

  override def payloadEmitter(p: Payload): Option[PartEmitter] =
    Some(Raml10PayloadPartEmitter(p, SpecOrdering.Lexical, Nil))

  override def endpointEmitter(e: EndPoint): Option[PartEmitter] = Some(Raml08EndPointEmitter(e, SpecOrdering.Lexical))
}

object Raml08EmitterFactory {
  def apply(eh: ErrorHandler): Raml08EmitterFactory = Raml08EmitterFactory()(new Raml08SpecEmitterContext(eh))
}

trait RamlEmitterFactory extends DomainElementEmitterFactory {

  implicit val ctx: RamlSpecEmitterContext

  override def exampleEmitter(example: Example): Option[PartEmitter] =
    Some(RamlExampleValuesEmitter(example, SpecOrdering.Lexical))

  override def traitEmitter(t: Trait): Option[PartEmitter] = Some(abstractDeclarationEmitter(t))

  override def resourceTypeEmitter(r: ResourceType): Option[PartEmitter] = Some(abstractDeclarationEmitter(r))

  private def abstractDeclarationEmitter(a: AbstractDeclaration): PartEmitter =
    AbstractDeclarationPartEmitter(a, SpecOrdering.Lexical, Nil)

  override def parametrizedSecuritySchemeEmitter(s: ParametrizedSecurityScheme): Option[PartEmitter] =
    Some(ctx.factory.parametrizedSecurityEmitter(s, SpecOrdering.Lexical))

  override def securityRequirementEmitter(s: SecurityRequirement): Option[PartEmitter] =
    Some(ctx.factory.securityRequirementEmitter(s, SpecOrdering.Lexical))

  override def creativeWorkEmitter(c: CreativeWork): Option[PartEmitter] =
    Some(RamlCreativeWorkEmitter(c, SpecOrdering.Lexical, withExtension = true))
}
