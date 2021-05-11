package amf.plugins.document.webapi.contexts.emitter.oas
import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter._
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.{Oas20, Oas30, Vendor}
import amf.plugins.document.webapi.contexts.emitter.jsonschema.JsonSchemaEmitterContext
import amf.plugins.document.webapi.contexts.emitter.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.plugins.document.webapi.parser.spec.declaration.SchemaPosition.Schema
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  OasLikeShapeEmitterContextAdapter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationTypeEmitter,
  FacetsInstanceEmitter,
  OasAnnotationTypeEmitter,
  OasFacetsInstanceEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypeEmitter
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.emitters._
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter}
import org.yaml.model.YDocument.PartBuilder

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

abstract class OasSpecEmitterContext(eh: ErrorHandler,
                                     refEmitter: RefEmitter = OasRefEmitter,
                                     options: ShapeRenderOptions = ShapeRenderOptions(),
                                     val compactEmission: Boolean = true)
    extends OasLikeSpecEmitterContext(eh, refEmitter, options)
    with CompactEmissionContext {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  override val factory: OasSpecEmitterFactory

  override def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] =
    super[CompactEmissionContext].filterLocal(elements)
}

class Oas3SpecEmitterContext(eh: ErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions(),
                             compactEmission: Boolean = true)
    extends OasSpecEmitterContext(eh, refEmitter, options, compactEmission) {
  override val anyOfKey: String                = "anyOf"
  val schemaVersion: SchemaVersion             = OAS30SchemaVersion(Schema)
  override val factory: OasSpecEmitterFactory  = Oas3SpecEmitterFactory(this)
  override val vendor: Vendor                  = Oas30
  override def schemasDeclarationsPath: String = "/components/schemas/"
}

class Oas2SpecEmitterContext(eh: ErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions(),
                             compactEmission: Boolean = true)
    extends OasSpecEmitterContext(eh, refEmitter, options, compactEmission) {
  val schemaVersion: SchemaVersion             = OAS20SchemaVersion(Schema)
  override val factory: OasSpecEmitterFactory  = new Oas2SpecEmitterFactory(this)
  override val vendor: Vendor                  = Oas20
  override def schemasDeclarationsPath: String = "/definitions/"
}

object OasRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
