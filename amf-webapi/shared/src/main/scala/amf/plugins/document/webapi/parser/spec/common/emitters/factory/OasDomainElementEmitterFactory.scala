package amf.plugins.document.webapi.parser.spec.common.emitters.factory

import amf.core.annotations.{DeclaredElement, DeclaredHeader, SynthesizedField}
import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{Annotations, Value}
import amf.plugins.document.webapi.annotations.{FormBodyParameter, RequiredParamPayload}
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterContext,
  Oas3SpecEmitterContext,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationEmitter
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
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.{Callback, Parameter, Payload, Request, Response, TemplatedLink}

case class Oas20EmitterFactory()(implicit val ctx: Oas2SpecEmitterContext) extends OasEmitterFactory {

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

object Oas20EmitterFactory {
  def apply(eh: ErrorHandler): Oas20EmitterFactory =
    Oas20EmitterFactory()(new Oas2SpecEmitterContext(eh, compactEmission = false))
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
}

object Oas30EmitterFactory {
  def apply(eh: ErrorHandler): Oas30EmitterFactory =
    Oas30EmitterFactory()(new Oas3SpecEmitterContext(eh, compactEmission = false))
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
}

trait OasLikeEmitterFactory extends DomainElementEmitterFactory {

  implicit val ctx: OasLikeSpecEmitterContext

  override def typeEmitter(s: Shape): Option[PartEmitter] = {
    Some(OasTypePartEmitter(s, SpecOrdering.Lexical, references = Nil))
  }

}
