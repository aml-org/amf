package amf.apicontract.internal.spec.raml.emitter

import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.spec.common.emitter
import amf.apicontract.internal.spec.common.emitter.{AbstractDeclarationPartEmitter, DomainElementEmitterFactory, EntryToPartEmitterAdapter, Raml08EndPointEmitter, Raml08OperationPartEmitter, Raml08ParameterPartEmitter, Raml10EndPointEmitter, Raml10OperationPartEmitter, Raml10ParameterPartEmitter}
import amf.apicontract.internal.spec.oas.emitter.{Raml10PayloadEmitter, Raml10PayloadPartEmitter}
import amf.apicontract.internal.spec.raml.emitter
import amf.apicontract.internal.spec.raml.emitter.context.{Raml08SpecEmitterContext, Raml10SpecEmitterContext, RamlSpecEmitterContext}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.plugins.document.apicontract.parser.spec.domain._
import amf.plugins.document.apicontract.parser.spec.raml.emitters.Raml08SecuritySchemeEmitter
import amf.shapes.internal.spec.contexts.emitter.raml.Raml08SpecEmitterContext

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
    Some(emitter.EntryToPartEmitterAdapter(Raml10PayloadEmitter(p, SpecOrdering.Lexical, Nil)))

  override def endpointEmitter(e: EndPoint): Option[PartEmitter] = Some(Raml10EndPointEmitter(e, SpecOrdering.Lexical))

  override def customDomainPropertyEmitter(c: CustomDomainProperty): Option[PartEmitter] =
    Some(new NamedPropertyTypeEmitter(c, Nil, SpecOrdering.Lexical))
}

object Raml10EmitterFactory {
  def apply(eh: AMFErrorHandler): Raml10EmitterFactory = Raml10EmitterFactory()(new Raml10SpecEmitterContext(eh))
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
  def apply(eh: AMFErrorHandler): Raml08EmitterFactory = Raml08EmitterFactory()(new Raml08SpecEmitterContext(eh))
}

trait RamlEmitterFactory extends DomainElementEmitterFactory {

  implicit val ctx: RamlSpecEmitterContext
  protected implicit val shapeCtx: RamlShapeEmitterContextAdapter = emitter.RamlShapeEmitterContextAdapter(ctx)

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
