package amf.plugins.document.webapi.contexts.emitter.raml
import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter._
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, DeclaresModel, Document}
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.{FieldEntry, Position}
import amf.core.remote.{Raml08, Raml10, Vendor}
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterContext,
  Oas2SpecEmitterFactory,
  OasRefEmitter,
  OasSpecEmitterFactory
}
import amf.plugins.document.webapi.contexts.{SpecEmitterContext, SpecEmitterFactory}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.RamlHeader
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{
  Raml08TypePartEmitter,
  Raml10TypePartEmitter,
  RamlTypePartEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  ApiShapeEmitterContextAdapter,
  RamlCustomFacetsEmitter,
  ShapeEmitterContext
}
import amf.plugins.document.webapi.parser.spec.domain.{RamlParameterEmitter, _}
import amf.plugins.document.webapi.parser.spec.raml.emitters.{
  Raml08NamedSecuritySchemeEmitter,
  Raml10NamedSecuritySchemeEmitter,
  RamlNamedSecuritySchemeEmitter
}
import amf.plugins.document.webapi.parser.spec.raml.{
  Raml08RootLevelEmitters,
  Raml10RootLevelEmitters,
  RamlRootLevelEmitters
}
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter, Response}
import amf.validations.RenderSideValidations.RenderValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YNode

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RamlEmitterVersionFactory extends SpecEmitterFactory {

  implicit val spec: RamlSpecEmitterContext
  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)

  def retrieveHeader(document: BaseUnit): Option[String]

  def endpointEmitter
    : (EndPoint, SpecOrdering, mutable.ListBuffer[RamlEndPointEmitter], Seq[BaseUnit]) => RamlEndPointEmitter

  def parameterEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => RamlParameterEmitter

  def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = parameterEmitter

  val typesKey: String

  def typesEmitter
    : (AnyShape, SpecOrdering, Option[AnnotationsEmitter], Seq[Field], Seq[BaseUnit]) => RamlTypePartEmitter

  def namedSecurityEmitter: (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlNamedSecuritySchemeEmitter

  def rootLevelEmitters: (BaseUnit with DeclaresModel, SpecOrdering) => RamlRootLevelEmitters

  override def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter =
    RamlTagToReferenceEmitter.apply

  override def parametrizedSecurityEmitter
    : (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter =
    RamlParametrizedSecuritySchemeEmitter.apply

  override def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => AbstractSecurityRequirementEmitter =
    RamlSecurityRequirementEmitter.apply

  def payloadsEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlPayloadsEmitter

  def responseEmitter: (Response, SpecOrdering, Seq[BaseUnit]) => RamlResponseEmitter

  def operationEmitter: (Operation, SpecOrdering, Seq[BaseUnit]) => RamlOperationEmitter

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    RamlDeclaredTypesEmitters.apply
}

class Raml10EmitterVersionFactory()(implicit val spec: RamlSpecEmitterContext) extends RamlEmitterVersionFactory {
  override def retrieveHeader(document: BaseUnit): Option[String] = document match {
    case _: Extension => Some(RamlHeader.Raml10Extension.text)
    case _: Overlay   => Some(RamlHeader.Raml10Overlay.text)
    case _: Document  => Some(RamlHeader.Raml10.text)
    case _ =>
      spec.eh.violation(RenderValidation,
                        document.id,
                        None,
                        "Document has no header.",
                        document.position(),
                        document.location())
      None
  }

  override def endpointEmitter
    : (EndPoint, SpecOrdering, ListBuffer[RamlEndPointEmitter], Seq[BaseUnit]) => RamlEndPointEmitter =
    Raml10EndPointEmitter.apply

  override val typesKey: String = "types"

  override def typesEmitter
    : (AnyShape, SpecOrdering, Option[AnnotationsEmitter], Seq[Field], Seq[BaseUnit]) => RamlTypePartEmitter =
    Raml10TypePartEmitter.apply

  override def namedSecurityEmitter: (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlNamedSecuritySchemeEmitter =
    Raml10NamedSecuritySchemeEmitter.apply

  override def rootLevelEmitters: (BaseUnit with DeclaresModel, SpecOrdering) => RamlRootLevelEmitters =
    Raml10RootLevelEmitters.apply

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter =
    RamlCustomFacetsEmitter.apply

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter =
    RamlFacetsInstanceEmitter.apply

  override def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter =
    RamlAnnotationEmitter.apply

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter =
    RamlAnnotationTypeEmitter.apply

  override def parameterEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => RamlParameterEmitter =
    Raml10ParameterEmitter.apply

  override def payloadsEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlPayloadsEmitter =
    Raml10PayloadsEmitter.apply

  override def operationEmitter: (Operation, SpecOrdering, Seq[BaseUnit]) => RamlOperationEmitter =
    Raml10OperationEmitter.apply

  override def responseEmitter: (Response, SpecOrdering, Seq[BaseUnit]) => RamlResponseEmitter =
    Raml10ResponseEmitter.apply
}

class Raml08EmitterVersionFactory()(implicit val spec: RamlSpecEmitterContext) extends RamlEmitterVersionFactory {
  override def retrieveHeader(document: BaseUnit): Option[String] = document match {
    case _: Document => Some(RamlHeader.Raml08.text)
    case _ =>
      spec.eh.violation(RenderValidation,
                        document.id,
                        None,
                        "Document has no header.",
                        document.position(),
                        document.location())
      None
  }

  override def endpointEmitter
    : (EndPoint, SpecOrdering, ListBuffer[RamlEndPointEmitter], Seq[BaseUnit]) => RamlEndPointEmitter =
    Raml08EndPointEmitter.apply

  override val typesKey: String = "schemas"

  override def typesEmitter
    : (AnyShape, SpecOrdering, Option[AnnotationsEmitter], Seq[Field], Seq[BaseUnit]) => RamlTypePartEmitter =
    Raml08TypePartEmitter.apply

  override def namedSecurityEmitter: (SecurityScheme, Seq[BaseUnit], SpecOrdering) => RamlNamedSecuritySchemeEmitter =
    Raml08NamedSecuritySchemeEmitter.apply

  override def rootLevelEmitters: (BaseUnit with DeclaresModel, SpecOrdering) => RamlRootLevelEmitters =
    Raml08RootLevelEmitters.apply

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter = {
    (f: FieldEntry, ordering: SpecOrdering, _: Seq[BaseUnit]) =>
      new CustomFacetsEmitter(f, ordering, Nil) {
        override val key: String                                                                = ""
        override def shapeEmitter: (PropertyShape, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = ???

        override def emit(b: EntryBuilder): Unit = {
          spec.eh.violation(
            RenderValidation,
            "",
            None,
            s"Custom facets not supported for vendor ${spec.vendor}",
            f.value.value.position(),
            f.value.value.location()
          )
        }
      }
  }

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter = {
    (shapeExtension: ShapeExtension, ordering: SpecOrdering) =>
      new FacetsInstanceEmitter(shapeExtension, ordering) {

        override def emit(b: EntryBuilder): Unit = {
          spec.eh.violation(
            RenderValidation,
            shapeExtension.id,
            None,
            s"Custom facets not supported for vendor ${spec.vendor}",
            shapeExtension.position(),
            shapeExtension.location()
          )
        }
        override val name: String = ""
      }
  }

  override def annotationEmitter: (DomainExtension, SpecOrdering) => AnnotationEmitter = {
    (domainExtension: DomainExtension, ordering: SpecOrdering) =>
      new AnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering) {

        override def emit(b: EntryBuilder): Unit = {
          spec.eh.violation(
            RenderValidation,
            domainExtension.id,
            None,
            s"Custom facets not supported for vendor ${spec.vendor}",
            domainExtension.position(),
            domainExtension.location()
          )
        }
        override val name: String = ""
      }
  }

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter = {
    (property: CustomDomainProperty, ordering: SpecOrdering) =>
      new AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering) {

        override def emitters(): Either[Seq[EntryEmitter], PartEmitter] =
          Left(shapeEmitters.asInstanceOf[Seq[EntryEmitter]])
        override protected val shapeEmitters: Seq[Emitter] = Seq(new EntryEmitter {
          override def emit(b: EntryBuilder): Unit = {
            spec.eh.violation(RenderValidation,
                              property.id,
                              None,
                              s"Custom facets not supported for vendor ${spec.vendor}",
                              property.position(),
                              property.location())
          }
          override def position(): Position = pos(property.annotations)
        })
      }
  }

  override def parameterEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => RamlParameterEmitter =
    Raml08ParameterEmitter.apply

  override def payloadsEmitter: (String, FieldEntry, SpecOrdering, Seq[BaseUnit]) => RamlPayloadsEmitter =
    Raml08PayloadsEmitter.apply

  override def operationEmitter: (Operation, SpecOrdering, Seq[BaseUnit]) => RamlOperationEmitter =
    Raml08OperationEmitter.apply

  override def responseEmitter: (Response, SpecOrdering, Seq[BaseUnit]) => RamlResponseEmitter =
    Raml08ResponseEmitter.apply

}

class Raml10SpecEmitterContext(eh: ErrorHandler,
                               refEmitter: RefEmitter = RamlRefEmitter,
                               options: ShapeRenderOptions = ShapeRenderOptions())
    extends RamlSpecEmitterContext(eh, refEmitter, options) {
  override val factory: RamlEmitterVersionFactory = new Raml10EmitterVersionFactory()(this)
  override val vendor: Vendor                     = Raml10
}

class XRaml10SpecEmitterContext(eh: ErrorHandler,
                                refEmitter: RefEmitter = OasRefEmitter,
                                options: ShapeRenderOptions = ShapeRenderOptions())
    extends Raml10SpecEmitterContext(eh, refEmitter, options) {
  override def localReference(reference: Linkable): PartEmitter =
    oasFactory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  val oasFactory: OasSpecEmitterFactory = new Oas2SpecEmitterFactory(
    new Oas2SpecEmitterContext(eh, refEmitter, options))
}

class Raml08SpecEmitterContext(eh: ErrorHandler, options: ShapeRenderOptions = ShapeRenderOptions())
    extends RamlSpecEmitterContext(eh, RamlRefEmitter, options) {
  override val factory: RamlEmitterVersionFactory = new Raml08EmitterVersionFactory()(this)
  override val vendor: Vendor                     = Raml08

}

abstract class RamlSpecEmitterContext(override val eh: ErrorHandler,
                                      refEmitter: RefEmitter,
                                      options: ShapeRenderOptions = ShapeRenderOptions())
    extends SpecEmitterContext(eh, refEmitter, options) {

  import BaseEmitters._

  override def localReference(reference: Linkable): PartEmitter = RamlLocalReferenceEmitter(reference)

  def localReferenceEntryEmitter(key: String, reference: Linkable): EntryEmitter =
    new RamlLocalReferenceEntryEmitter(key, reference)

  def externalReference(reference: Linkable): PartEmitter =
    new PartEmitter {
      override def emit(b: PartBuilder): Unit =
        b += YNode.include(reference.linkLabel.option().getOrElse(reference.location().get))
      override def position(): Position = pos(reference.annotations)
    }

  val factory: RamlEmitterVersionFactory

}

object RamlRefEmitter extends RefEmitter {
  override def ref(url: String, b: PartBuilder): Unit = b += YNode.include(url)
}
