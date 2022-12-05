package amf.apicontract.internal.spec.oas.emitter.context

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.security.{
  ParametrizedSecurityScheme,
  SecurityRequirement,
  SecurityScheme
}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.apicontract.internal.spec.common.emitter._
import amf.apicontract.internal.spec.jsonschema.JsonSchemaEmitterContext
import amf.apicontract.internal.spec.oas.emitter.domain._
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.{Oas20, Oas30, Spec}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters._
import amf.shapes.internal.spec.common.emitter.annotations.{
  AnnotationEmitter,
  FacetsInstanceEmitter,
  OasAnnotationEmitter,
  OasFacetsInstanceEmitter
}
import amf.shapes.internal.spec.common.emitter._
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, OAS30SchemaVersion, SchemaPosition, SchemaVersion}
import amf.shapes.internal.spec.contexts.emitter.oas.{CompactableEmissionContext, OasCompactEmitterFactory}
import amf.shapes.internal.spec.oas.emitter.OasTypeEmitter

abstract class OasSpecEmitterFactory(override implicit val spec: OasSpecEmitterContext)
    extends OasLikeSpecEmitterFactory
    with OasCompactEmitterFactory {

  protected override implicit val shapeCtx: OasLikeShapeEmitterContextAdapter = OasLikeShapeEmitterContextAdapter(spec)

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

  def serversEmitter(
      operation: Operation,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): OasServersEmitter

  def serversEmitter(
      endpoint: EndPoint,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): OasServersEmitter

  def headerEmitter: (Parameter, SpecOrdering, Seq[BaseUnit]) => EntryEmitter = OasHeaderEmitter.apply

}

class Oas2SpecEmitterFactory(override val spec: OasSpecEmitterContext) extends OasSpecEmitterFactory()(spec) {
  override def serversEmitter(
      api: Api,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): Oas2ServersEmitter =
    Oas2ServersEmitter(api, f, ordering, references)(spec)

  override def serversEmitter(
      operation: Operation,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): Oas3OperationServersEmitter =
    Oas3OperationServersEmitter(operation, f, ordering, references)(spec)

  override def serversEmitter(
      endpoint: EndPoint,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): OasServersEmitter =
    Oas3EndPointServersEmitter(endpoint, f, ordering, references)(spec)

  override def securitySchemesEmitters(
      securitySchemes: Seq[SecurityScheme],
      ordering: SpecOrdering
  ): OasSecuritySchemesEmitters =
    new Oas2SecuritySchemesEmitters(securitySchemes, ordering)(spec)
}

/** Overrides type emitter to avoid extracting nested types to definitions. Uses compact emission to use dynamic queue
  * when emitting recursive shapes.
  */
case class InlinedJsonSchemaEmitterFactory()(override implicit val spec: JsonSchemaEmitterContext)
    extends Oas2SpecEmitterFactory(spec) {

  override def typeEmitters(
      shape: Shape,
      ordering: SpecOrdering,
      ignored: Seq[Field],
      references: Seq[BaseUnit],
      pointer: Seq[String],
      schemaPath: Seq[(String, String)]
  ): Seq[Emitter] =
    OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()

}

case class Oas3SpecEmitterFactory(override val spec: OasSpecEmitterContext) extends OasSpecEmitterFactory()(spec) {
  override def serversEmitter(
      api: Api,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): Oas3WebApiServersEmitter =
    Oas3WebApiServersEmitter(api, f, ordering, references)(spec)

  override def serversEmitter(
      operation: Operation,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): Oas3OperationServersEmitter =
    Oas3OperationServersEmitter(operation, f, ordering, references)(spec)

  override def serversEmitter(
      endpoint: EndPoint,
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): OasServersEmitter =
    Oas3EndPointServersEmitter(endpoint, f, ordering, references)(spec)

  override def securitySchemesEmitters(
      securitySchemes: Seq[SecurityScheme],
      ordering: SpecOrdering
  ): OasSecuritySchemesEmitters =
    new Oas3SecuritySchemesEmitters(securitySchemes, ordering)(spec)
}

abstract class OasSpecEmitterContext(
    eh: AMFErrorHandler,
    refEmitter: RefEmitter = OasRefEmitter,
    renderConfig: RenderConfiguration
) extends OasLikeSpecEmitterContext(eh, refEmitter, renderConfig)
    with CompactableEmissionContext {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], Nil)

  override val factory: OasSpecEmitterFactory
}

class Oas3SpecEmitterContext(eh: AMFErrorHandler, refEmitter: RefEmitter = OasRefEmitter, config: RenderConfiguration)
    extends OasSpecEmitterContext(eh, refEmitter, config) {
  override val anyOfKey: String                = "anyOf"
  val schemaVersion: SchemaVersion             = OAS30SchemaVersion(SchemaPosition.Schema)
  override val factory: OasSpecEmitterFactory  = Oas3SpecEmitterFactory(this)
  override val spec: Spec                      = Oas30
  override def schemasDeclarationsPath: String = "/components/schemas/"
}

class Oas2SpecEmitterContext(eh: AMFErrorHandler, refEmitter: RefEmitter = OasRefEmitter, config: RenderConfiguration)
    extends OasSpecEmitterContext(eh, refEmitter, config) {
  override val anyOfKey: String                = "anyOf"
  val schemaVersion: SchemaVersion             = OAS20SchemaVersion(SchemaPosition.Schema)
  override val factory: OasSpecEmitterFactory  = new Oas2SpecEmitterFactory(this)
  override val spec: Spec                      = Oas20
  override def schemasDeclarationsPath: String = "/definitions/"
}
