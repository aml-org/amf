package amf.apicontract.internal.spec.raml.emitter.context

import amf.apicontract.client.scala.model.document.{Extension, Overlay}
import amf.apicontract.client.scala.model.domain.security.{
  ParametrizedSecurityScheme,
  SecurityRequirement,
  SecurityScheme
}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter, Response}
import amf.apicontract.internal.spec.common.emitter._
import amf.apicontract.internal.spec.oas.emitter.context.{
  Oas2SpecEmitterContext,
  Oas2SpecEmitterFactory,
  OasSpecEmitterFactory
}
import amf.apicontract.internal.spec.oas.emitter.domain.{
  Raml08PayloadsEmitter,
  Raml10PayloadsEmitter,
  RamlPayloadsEmitter
}
import amf.apicontract.internal.spec.raml.RamlHeader
import amf.apicontract.internal.spec.raml.emitter._
import amf.apicontract.internal.spec.raml.emitter.document.{
  Raml08RootLevelEmitters,
  Raml10RootLevelEmitters,
  RamlRootLevelEmitters
}
import amf.apicontract.internal.spec.raml.emitter.domain._
import amf.core.client.common.position.Position
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel, Document}
import amf.core.client.scala.model.domain.extensions.{
  CustomDomainProperty,
  DomainExtension,
  PropertyShape,
  ShapeExtension
}
import amf.core.client.scala.model.domain.{CustomizableElement, DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.{Raml08, Raml10, Spec}
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.core.internal.validation.RenderSideValidations.RenderValidation
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter._
import amf.shapes.internal.spec.common.emitter.annotations._
import amf.shapes.internal.spec.common.{RAML08SchemaVersion, RAML10SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.raml.emitter.{Raml08TypePartEmitter, Raml10TypePartEmitter, RamlTypePartEmitter}
import amf.shapes.internal.spec.raml.parser.{RamlLocalReferenceEmitter, RamlTagToReferenceEmitter}
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RamlEmitterVersionFactory extends SpecEmitterFactory {

  implicit val spec: RamlSpecEmitterContext
  protected implicit val shapeContext: RamlShapeEmitterContext = RamlShapeEmitterContextAdapter(spec)

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

  override def annotationEmitter: (CustomizableElement, DomainExtension, SpecOrdering) => AnnotationEmitter =
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
            s"Custom facets not supported for spec ${spec.spec}",
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
            s"Custom facets not supported for spec ${spec.spec}",
            shapeExtension.position(),
            shapeExtension.location()
          )
        }
        override val name: String = ""
      }
  }

  override def annotationEmitter: (CustomizableElement, DomainExtension, SpecOrdering) => AnnotationEmitter = {
    (elem: CustomizableElement, domainExtension: DomainExtension, ordering: SpecOrdering) =>
      new AnnotationEmitter(elem, domainExtension: DomainExtension, ordering: SpecOrdering, _ => "") {

        override def emit(b: EntryBuilder): Unit = {
          spec.eh.violation(
            RenderValidation,
            domainExtension.id,
            None,
            s"Custom facets not supported for spec ${spec.spec}",
            domainExtension.position(),
            domainExtension.location()
          )
        }
      }
  }

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter = {
    (property: CustomDomainProperty, ordering: SpecOrdering) =>
      new AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering) {

        override protected implicit val shapeCtx: ShapeEmitterContext = shapeContext

        override def emitters(): Either[Seq[EntryEmitter], PartEmitter] =
          Left(shapeEmitters.asInstanceOf[Seq[EntryEmitter]])
        override protected val shapeEmitters: Seq[Emitter] = Seq(new EntryEmitter {
          override def emit(b: EntryBuilder): Unit = {
            spec.eh.violation(RenderValidation,
                              property.id,
                              None,
                              s"Custom facets not supported for spec ${spec.spec}",
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

class Raml10SpecEmitterContext(eh: AMFErrorHandler,
                               refEmitter: RefEmitter = RamlRefEmitter,
                               config: RenderConfiguration)
    extends RamlSpecEmitterContext(eh, refEmitter, config) {
  override val factory: RamlEmitterVersionFactory = new Raml10EmitterVersionFactory()(this)
  override val spec: Spec                         = Raml10

  override def schemaVersion: SchemaVersion = RAML10SchemaVersion
}

class XRaml10SpecEmitterContext(eh: AMFErrorHandler,
                                refEmitter: RefEmitter = OasRefEmitter,
                                renderConfig: RenderConfiguration)
    extends Raml10SpecEmitterContext(eh, refEmitter, renderConfig) {
  override def localReference(reference: Linkable): PartEmitter =
    oasFactory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  val oasFactory: OasSpecEmitterFactory = new Oas2SpecEmitterFactory(
    new Oas2SpecEmitterContext(eh, refEmitter, renderConfig))

  override def schemaVersion: SchemaVersion = RAML10SchemaVersion
}

class Raml08SpecEmitterContext(eh: AMFErrorHandler, renderConfig: RenderConfiguration)
    extends RamlSpecEmitterContext(eh, RamlRefEmitter, renderConfig) {
  override val factory: RamlEmitterVersionFactory = new Raml08EmitterVersionFactory()(this)
  override val spec: Spec                         = Raml08

  override def schemaVersion: SchemaVersion = RAML08SchemaVersion
}

abstract class RamlSpecEmitterContext(override val eh: AMFErrorHandler,
                                      refEmitter: RefEmitter,
                                      renderConfig: RenderConfiguration)
    extends SpecEmitterContext(eh, refEmitter, renderConfig) {

  override def localReference(reference: Linkable): PartEmitter = RamlLocalReferenceEmitter(reference)

  val factory: RamlEmitterVersionFactory

}
