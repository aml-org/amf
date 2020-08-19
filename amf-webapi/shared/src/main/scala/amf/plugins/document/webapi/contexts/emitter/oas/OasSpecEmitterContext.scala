package amf.plugins.document.webapi.contexts.emitter.oas
import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter._
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{CustomDomainProperty, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.{Oas20, Oas30, Vendor}
import amf.plugins.document.webapi.contexts.emitter.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.plugins.document.webapi.contexts.{RefEmitter, TagToReferenceEmitter}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.{OasRecursiveShapeEmitter, OasTypeEmitter}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.emitters._
import amf.plugins.document.webapi.parser.{
  CommonOasTypeDefMatcher,
  JsonSchemaTypeDefMatcher,
  OasTypeDefStringValueMatcher
}
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter, WebApi}
import org.yaml.model.YDocument.PartBuilder

import scala.util.matching.Regex

abstract class OasSpecEmitterFactory(override implicit val spec: OasSpecEmitterContext)
    extends OasLikeSpecEmitterFactory
    with OasCompactEmitterFactory {
  override def tagToReferenceEmitter: (DomainElement, Option[String], Seq[BaseUnit]) => TagToReferenceEmitter =
    (domainElement, label, _) => OasTagToReferenceEmitter(domainElement, label)

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

  def serversEmitter(api: WebApi, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): OasServersEmitter

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

abstract class InlinedOasSpecEmitterFactory(override implicit val spec: OasSpecEmitterContext)
    extends OasSpecEmitterFactory {

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
      OasDeclaredTypesEmitters.apply

  override def typeEmitters(shape: Shape,
                            ordering: SpecOrdering,
                            ignored: Seq[Field] = Nil,
                            references: Seq[BaseUnit],
                            pointer: Seq[String] = Nil,
                            schemaPath: Seq[(String, String)] = Nil): Seq[Emitter] =
    OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()

  override def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter =
    OasRecursiveShapeEmitter.apply
}

trait Oas2SpecEmitterFactoryBase {
  implicit val spec: OasSpecEmitterContext

  def serversEmitter(api: WebApi,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): Oas2ServersEmitter =
    Oas2ServersEmitter(api, f, ordering, references)(spec)

  def serversEmitter(operation: Operation,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): Oas3OperationServersEmitter =
    Oas3OperationServersEmitter(operation, f, ordering, references)(spec)

  def serversEmitter(endpoint: EndPoint,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): OasServersEmitter =
    Oas3EndPointServersEmitter(endpoint, f, ordering, references)(spec)

  def securitySchemesEmitters(securitySchemes: Seq[SecurityScheme],
                              ordering: SpecOrdering): OasSecuritySchemesEmitters =
    new Oas2SecuritySchemesEmitters(securitySchemes, ordering)(spec)
}

class Oas2SpecEmitterFactory(override val spec: OasSpecEmitterContext)
    extends OasSpecEmitterFactory()(spec)
    with Oas2SpecEmitterFactoryBase

class InlinedOas2SpecEmitterFactory(override val spec: OasSpecEmitterContext)
    extends InlinedOasSpecEmitterFactory()(spec)
    with Oas2SpecEmitterFactoryBase

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

trait Oas3SpecEmitterFactoryBase {
  implicit val spec: OasSpecEmitterContext

  def serversEmitter(api: WebApi,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): Oas3WebApiServersEmitter =
    Oas3WebApiServersEmitter(api, f, ordering, references)(spec)

  def serversEmitter(operation: Operation,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): Oas3OperationServersEmitter =
    Oas3OperationServersEmitter(operation, f, ordering, references)(spec)

  def serversEmitter(endpoint: EndPoint,
                     f: FieldEntry,
                     ordering: SpecOrdering,
                     references: Seq[BaseUnit]): OasServersEmitter =
    Oas3EndPointServersEmitter(endpoint, f, ordering, references)(spec)

  def securitySchemesEmitters(securitySchemes: Seq[SecurityScheme],
                              ordering: SpecOrdering): OasSecuritySchemesEmitters =
    new Oas3SecuritySchemesEmitters(securitySchemes, ordering)(spec)
}

case class Oas3SpecEmitterFactory(override val spec: OasSpecEmitterContext)
    extends OasSpecEmitterFactory()(spec)
    with Oas3SpecEmitterFactoryBase

case class InlinedOas3SpecEmitterFactory(override val spec: OasSpecEmitterContext)
    extends InlinedOasSpecEmitterFactory()(spec)
    with Oas3SpecEmitterFactoryBase

abstract class OasSpecEmitterContext(eh: ErrorHandler,
                                     refEmitter: RefEmitter = OasRefEmitter,
                                     options: ShapeRenderOptions = ShapeRenderOptions().withCompactedEmission,
                                     val compactEmission: Boolean = true)
    extends OasLikeSpecEmitterContext(eh, refEmitter, options)
    with CompactEmissionContext {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], reference.linkLabel.option(), Nil)

  override val factory: OasSpecEmitterFactory

  val typeDefMatcher: OasTypeDefStringValueMatcher = CommonOasTypeDefMatcher

  override def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] =
    super[CompactEmissionContext].filterLocal(elements)
}

class JsonSchemaEmitterContext(override val eh: ErrorHandler,
                               override val options: ShapeRenderOptions = ShapeRenderOptions(),
                               override val schemaVersion: JSONSchemaVersion)
    extends Oas2SpecEmitterContext(eh = eh, options = options) {
  override val typeDefMatcher: OasTypeDefStringValueMatcher = JsonSchemaTypeDefMatcher

  override val anyOfKey: String                = "anyOf"
  override val nameRegex: Regex                = """^[a-zA-Z0-9\.\-_]+$""".r
  override def schemasDeclarationsPath: String = "/definitions/"
}

object JsonSchemaEmitterContext {
  def apply(eh: ErrorHandler, options: ShapeRenderOptions): JsonSchemaEmitterContext =
    new JsonSchemaEmitterContext(eh, options, OAS20SchemaVersion("schema")(eh))
}

final case class InlinedJsonSchemaEmitterContext(override val eh: ErrorHandler,
                                                 override val options: ShapeRenderOptions = ShapeRenderOptions(),
                                                 override val schemaVersion: JSONSchemaVersion)
    extends JsonSchemaEmitterContext(eh = eh, options = options, schemaVersion) {
  override val factory: OasSpecEmitterFactory = InlinedJsonSchemaEmitterFactory()(this)
}

object InlinedJsonSchemaEmitterContext {
  def apply(eh: ErrorHandler, options: ShapeRenderOptions): InlinedJsonSchemaEmitterContext =
    InlinedJsonSchemaEmitterContext(eh, options, schemaVersion = OAS20SchemaVersion("schema")(eh))
}

class Oas3SpecEmitterContext(eh: ErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions().withCompactedEmission,
                             compactEmission: Boolean = true)
    extends OasSpecEmitterContext(eh, refEmitter, options, compactEmission) {
  override val anyOfKey: String                = "anyOf"
  val schemaVersion: JSONSchemaVersion         = OAS30SchemaVersion("schema")(eh)
  override val vendor: Vendor                  = Oas30
  override def schemasDeclarationsPath: String = "/components/schemas/"
  override val factory: OasSpecEmitterFactory  = Oas3SpecEmitterFactory(this)
}

class InlinedOas3SpecEmitterContext(eh: ErrorHandler,
                                    refEmitter: RefEmitter = OasRefEmitter,
                                    options: ShapeRenderOptions = ShapeRenderOptions().withCompactedEmission,
                                    compactEmission: Boolean = true)
    extends Oas3SpecEmitterContext(eh, refEmitter, options, compactEmission) {
  override val factory: OasSpecEmitterFactory  = InlinedOas3SpecEmitterFactory(this)
}

class Oas2SpecEmitterContext(eh: ErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions().withCompactedEmission,
                             compactEmission: Boolean = true)
    extends OasSpecEmitterContext(eh, refEmitter, options, compactEmission) {
  val schemaVersion: JSONSchemaVersion         = OAS20SchemaVersion("schema")(eh)
  override val vendor: Vendor                  = Oas20
  override def schemasDeclarationsPath: String = "/definitions/"
  override val factory: OasSpecEmitterFactory  = new Oas2SpecEmitterFactory(this)
}

class InlinedOas2SpecEmitterContext(eh: ErrorHandler,
                                    refEmitter: RefEmitter = OasRefEmitter,
                                    options: ShapeRenderOptions = ShapeRenderOptions().withCompactedEmission,
                                    compactEmission: Boolean = true)
    extends Oas2SpecEmitterContext(eh, refEmitter, options, compactEmission) {
  override val factory: OasSpecEmitterFactory  = new InlinedOas2SpecEmitterFactory(this)
}

object OasRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
