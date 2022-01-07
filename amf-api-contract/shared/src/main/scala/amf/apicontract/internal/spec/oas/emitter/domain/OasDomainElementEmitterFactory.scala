package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.security.{
  ParametrizedSecurityScheme,
  SecurityRequirement,
  SecurityScheme
}
import amf.apicontract.internal.annotations.{FormBodyParameter, RequiredParamPayload}
import amf.apicontract.internal.metamodel.domain.ParameterModel
import amf.apicontract.internal.spec.common.emitter._
import amf.apicontract.internal.spec.oas.OasLikeSecuritySchemeTypeMappings
import amf.apicontract.internal.spec.oas.emitter.context._
import amf.apicontract.internal.spec.oas.emitter.document.{EndPointPartEmitter, Oas3RequestBodyPartEmitter}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.internal.annotations.{DeclaredElement, DeclaredHeader, SynthesizedField}
import amf.core.internal.parser.domain.{Annotations, Value}
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.client.scala.model.domain.{CreativeWork, Example}
import amf.shapes.internal.spec.common.emitter.{
  ExampleDataNodePartEmitter,
  Oas3ExampleValuesPartEmitter,
  OasCreativeWorkEmitter
}
import amf.shapes.internal.spec.oas.emitter.OasTypePartEmitter

case class Oas20EmitterFactory()(implicit val ctx: Oas2SpecEmitterContext) extends OasEmitterFactory {

  override def payloadEmitter(p: Payload): Option[PartEmitter] = {
    if (isParamPayload(p))
      Some(PayloadAsParameterEmitter(p, SpecOrdering.Lexical, Nil))
    else Some(OasPayloadEmitter(p, SpecOrdering.Lexical, Nil))
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

  override def securitySchemeEmitter(s: SecurityScheme): Option[PartEmitter] =
    Some(
      new OasSecuritySchemeEmitter(s,
                                   OasLikeSecuritySchemeTypeMappings.mapsTo(ctx.spec, s.`type`.value()),
                                   SpecOrdering.Lexical))

  override def exampleEmitter(example: Example): Option[PartEmitter] =
    Some(ExampleDataNodePartEmitter(example, SpecOrdering.Lexical))
}

object Oas20EmitterFactory {
  def apply(eh: AMFErrorHandler, config: RenderConfiguration): Oas20EmitterFactory =
    Oas20EmitterFactory()(
      new Oas2SpecEmitterContext(eh, config = config.withRenderOptions(RenderOptions().withoutCompactedEmission)))
}

case class Oas30EmitterFactory()(implicit val ctx: Oas3SpecEmitterContext) extends OasEmitterFactory {

  override def exampleEmitter(example: Example): Option[PartEmitter] =
    Some(Oas3ExampleValuesPartEmitter(example, SpecOrdering.Lexical))

  override def templatedLinkEmitter(link: TemplatedLink): Option[PartEmitter] =
    Some(OasLinkPartEmitter(link, SpecOrdering.Lexical, Nil))

  override def callbackEmitter(callback: Callback): Option[PartEmitter] =
    Some(OasCallbackEmitter(List(callback), SpecOrdering.Lexical, Nil))

  override def requestEmitter(r: Request): Option[PartEmitter] =
    Some(Oas3RequestBodyPartEmitter(r, SpecOrdering.Lexical, Nil))

  override def securitySchemeEmitter(s: SecurityScheme): Option[PartEmitter] =
    Some(
      Oas3SecuritySchemeEmitter(s,
                                OasLikeSecuritySchemeTypeMappings.mapsTo(ctx.spec, s.`type`.value()),
                                SpecOrdering.Lexical))

  override def payloadEmitter(p: Payload): Option[PartEmitter] =
    Some(OasPayloadEmitter(p, SpecOrdering.Lexical, Nil))
}

object Oas30EmitterFactory {
  def apply(eh: AMFErrorHandler, config: RenderConfiguration): Oas30EmitterFactory =
    Oas30EmitterFactory()(
      new Oas3SpecEmitterContext(eh, config = config.withRenderOptions(RenderOptions().withoutCompactedEmission)))
}

trait OasEmitterFactory extends OasLikeEmitterFactory {

  implicit val ctx: OasSpecEmitterContext

  override def parameterEmitter(p: Parameter): Option[PartEmitter] = {
    val isHeader = isExplicitHeader(p)
    Some(ParameterEmitter(p, SpecOrdering.Lexical, Nil, asHeader = isHeader))
  }

  private def isExplicitHeader(p: Parameter): Boolean =
    p.annotations.contains(classOf[DeclaredHeader]) || {
      val bindingValue = p.fields.getValueAsOption(ParameterModel.Binding)
      bindingValue.exists {
        case Value(v: AmfScalar, a: Annotations) =>
          a.contains(classOf[SynthesizedField]) && v.toString == "header"
      }
    }

  override def responseEmitter(r: Response): Option[PartEmitter] =
    Some(OasResponsePartEmitter(r, SpecOrdering.Lexical, Nil))

  override def serverEmitter(s: Server): Option[PartEmitter] = Some(OasServerEmitter(s, SpecOrdering.Lexical))

  override def endpointEmitter(e: EndPoint): Option[PartEmitter] =
    Some(EndPointPartEmitter(e, SpecOrdering.Lexical, Nil))
}

trait OasLikeEmitterFactory extends DomainElementEmitterFactory {

  implicit val ctx: OasLikeSpecEmitterContext
  protected implicit val shapeCtx: OasLikeShapeEmitterContextAdapter = OasLikeShapeEmitterContextAdapter(ctx)

  override def typeEmitter(s: Shape): Option[PartEmitter] =
    Some(OasTypePartEmitter(s, SpecOrdering.Lexical, references = Nil))

  override def parametrizedSecuritySchemeEmitter(s: ParametrizedSecurityScheme): Option[PartEmitter] =
    Some(ctx.factory.parametrizedSecurityEmitter(s, SpecOrdering.Lexical))

  override def securityRequirementEmitter(s: SecurityRequirement): Option[PartEmitter] =
    Some(ctx.factory.securityRequirementEmitter(s, SpecOrdering.Lexical))

  override def creativeWorkEmitter(c: CreativeWork): Option[PartEmitter] =
    Some(OasCreativeWorkEmitter(c, SpecOrdering.Lexical))

  override def licenseEmitter(l: License): Option[PartEmitter] = Some(LicensePartEmitter(l, SpecOrdering.Lexical))

  override def organizationEmitter(o: Organization): Option[PartEmitter] =
    Some(OrganizationPartEmitter(o, SpecOrdering.Lexical))

  override def tagEmitter(t: Tag): Option[PartEmitter] = Some(TagEmitter(t, SpecOrdering.Lexical))
}
