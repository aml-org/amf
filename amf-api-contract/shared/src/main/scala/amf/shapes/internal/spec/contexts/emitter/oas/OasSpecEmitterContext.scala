package amf.shapes.internal.spec.contexts.emitter.oas
import amf.core.client.scala.config.ShapeRenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.{Oas20, Oas30, Vendor}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters._
import amf.shapes.internal.spec.contexts.emitter.jsonschema.JsonSchemaEmitterContext
import amf.shapes.internal.spec.contexts.emitter.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.plugins.document.apicontract.parser.spec.declaration.SchemaPosition.Schema
import amf.plugins.document.apicontract.parser.spec.declaration._
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContextAdapter
import amf.shapes.internal.spec.common.emitter.annotations.{
  AnnotationTypeEmitter,
  FacetsInstanceEmitter,
  OasAnnotationTypeEmitter,
  OasFacetsInstanceEmitter
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas.OasTypeEmitter
import amf.plugins.document.apicontract.parser.spec.domain._
import amf.plugins.document.apicontract.parser.spec.oas.emitters._
import amf.plugins.domain.apicontract.models.api.Api
import amf.plugins.domain.apicontract.models.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.plugins.domain.apicontract.models.{EndPoint, Operation, Parameter}

abstract class OasSpecEmitterFactory(override implicit val spec: OasSpecEmitterContext)
    extends OasLikeSpecEmitterFactory
    with OasCompactEmitterFactory {

  protected override implicit val shapeCtx = OasLikeShapeEmitterContextAdapter(spec)

  override def tagToReferenceEmitter: (DomainElement, Seq[BaseUnit]) => TagToReferenceEmitter =
    (link, _) => OasTagToReferenceEmitter(link)

  override def customFacetsEmitter: (FieldEntry, SpecOrdering, Seq[BaseUnit]) => CustomFacetsEmitter =
    OasCustomFacetsEmitter.apply

  override def facetsInstanceEmitter: (ShapeExtension, SpecOrdering) => FacetsInstanceEmitter =
    OasFacetsInstanceEmitter.apply

  override def securityRequirementEmitter: (SecurityRequirement, SpecOrdering) => AbstractSecurityRequirementEmitter =
    OasSecurityRequirementEmitter.apply

  override def parametrizedSecurityEmitter
    : (ParametrizedSecurityScheme, SpecOrdering) => ParametrizedSecuritySchemeEmitter =
    OasParametrizedSecuritySchemeEmitter.apply

  override def annotationTypeEmitter: (CustomDomainProperty, SpecOrdering) => AnnotationTypeEmitter =
    OasAnnotationTypeEmitter.apply

  def securitySchemesEmitters(securitySchemes: Seq[SecurityScheme], ordering: SpecOrdering): OasSecuritySchemesEmitters

  def serversEmitter(api: Api, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): OasServersEmitter

  def serversEmitter(operation: Operation,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): OasServersEmitter

  def serversEmitter(endpoint: EndPoint,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): OasServersEmitter

  def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = OasHeaderEmitter.apply

}

class Oas2SpecEmitterFactory(override val spec: OasSpecEmitterContext) extends OasSpecEmitterFactory()(spec) {
  override def serversEmitter(api: Api,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit]): Oas2ServersEmitter =
    Oas2ServersEmitter(api, f, ordering, references)(spec)

  override def serversEmitter(operation: Operation,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit]): Oas3OperationServersEmitter =
    Oas3OperationServersEmitter(operation, f, ordering, references)(spec)

  override def serversEmitter(endpoint: EndPoint,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit]): OasServersEmitter =
    Oas3EndPointServersEmitter(endpoint, f, ordering, references)(spec)

  override def securitySchemesEmitters(securitySchemes: Seq[SecurityScheme],
                                       ordering: SpecOrdering): OasSecuritySchemesEmitters =
    new Oas2SecuritySchemesEmitters(securitySchemes, ordering)(spec)
}

/**
  * Overrides type emitter to avoid extracting nested types to definitions.
  * Uses compact emission to use dynamic queue when emitting recursive shapes.
  */
case class InlinedJsonSchemaEmitterFactory()(override implicit val spec: JsonSchemaEmitterContext)
    extends Oas2SpecEmitterFactory(spec) {

  override def typeEmitters(shape: Shape,
                            ordering: SpecOrdering,
                            ignored: Seq[Field],
                            references: Seq[BaseUnit],
                            pointer: Seq[String],
                            schemaPath: Seq[(String, String)]): Seq[Emitter] =
    OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()

}

case class Oas3SpecEmitterFactory(override val spec: OasSpecEmitterContext) extends OasSpecEmitterFactory()(spec) {
  override def serversEmitter(api: Api,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit]): Oas3WebApiServersEmitter =
    Oas3WebApiServersEmitter(api, f, ordering, references)(spec)

  override def serversEmitter(operation: Operation,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit]): Oas3OperationServersEmitter =
    Oas3OperationServersEmitter(operation, f, ordering, references)(spec)

  override def serversEmitter(endpoint: EndPoint,
                              f: FieldEntry,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit]): OasServersEmitter =
    Oas3EndPointServersEmitter(endpoint, f, ordering, references)(spec)

  override def securitySchemesEmitters(securitySchemes: Seq[SecurityScheme],
                                       ordering: SpecOrdering): OasSecuritySchemesEmitters =
    new Oas3SecuritySchemesEmitters(securitySchemes, ordering)(spec)
}

abstract class OasSpecEmitterContext(eh: AMFErrorHandler,
                                     refEmitter: RefEmitter = OasRefEmitter,
                                     options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasLikeSpecEmitterContext(eh, refEmitter, options)
    with CompactableEmissionContext {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  override val factory: OasSpecEmitterFactory

  override def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] =
    super[CompactableEmissionContext].filterLocal(elements)
}

class Oas3SpecEmitterContext(eh: AMFErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasSpecEmitterContext(eh, refEmitter, options) {
  override val anyOfKey: String                = "anyOf"
  val schemaVersion: SchemaVersion             = OAS30SchemaVersion(Schema)
  override val factory: OasSpecEmitterFactory  = Oas3SpecEmitterFactory(this)
  override val vendor: Vendor                  = Oas30
  override def schemasDeclarationsPath: String = "/components/schemas/"
}

class Oas2SpecEmitterContext(eh: AMFErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasSpecEmitterContext(eh, refEmitter, options) {
  val schemaVersion: SchemaVersion             = OAS20SchemaVersion(Schema)
  override val factory: OasSpecEmitterFactory  = new Oas2SpecEmitterFactory(this)
  override val vendor: Vendor                  = Oas20
  override def schemasDeclarationsPath: String = "/definitions/"
}
