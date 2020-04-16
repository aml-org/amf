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
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.emitter.oas.DefinitionsEmissionHelper.{Id, Label, normalizeName}
import amf.plugins.document.webapi.contexts.emitter.{OasLikeSpecEmitterContext, OasLikeSpecEmitterFactory}
import amf.plugins.document.webapi.contexts.{RefEmitter, TagToReferenceEmitter}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.schema.json.{
  CompactJsonSchemaRecursiveShapeEmitter,
  CompactJsonSchemaTypeEmitter
}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.emitters._
import amf.plugins.document.webapi.parser.{
  CommonOasTypeDefMatcher,
  JsonSchemaTypeDefMatcher,
  OasTypeDefStringValueMatcher
}
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter, WebApi}
import org.yaml.model.YDocument.PartBuilder

import scala.collection.mutable
import scala.util.matching.Regex

abstract class OasSpecEmitterFactory(override implicit val spec: OasSpecEmitterContext)
    extends OasLikeSpecEmitterFactory {
  override def tagToReferenceEmitter: (DomainElement, Option[String], Seq[BaseUnit]) => TagToReferenceEmitter =
    OasTagToReferenceEmitter.apply

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

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    OasDeclaredTypesEmitters.apply
}

class Oas2SpecEmitterFactory(override val spec: OasSpecEmitterContext) extends OasSpecEmitterFactory()(spec) {
  override def serversEmitter(api: WebApi,
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

case class CompactJsonSchemaEmitterFactory()(override implicit val spec: CompactJsonSchemaEmitterContext)
    extends Oas2SpecEmitterFactory(spec) {
  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    CompactJsonSchemaTypesEmitters.apply

  override def typeEmitters(shape: Shape,
                            ordering: SpecOrdering,
                            ignored: Seq[Field],
                            references: Seq[BaseUnit],
                            pointer: Seq[String],
                            schemaPath: Seq[(String, String)]): Seq[Emitter] =
    CompactJsonSchemaTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()

  override def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter =
    (shape, ordering, list) => new CompactJsonSchemaRecursiveShapeEmitter(shape, ordering, list)
}

case class Oas3SpecEmitterFactory(override val spec: OasSpecEmitterContext) extends OasSpecEmitterFactory()(spec) {
  override def serversEmitter(api: WebApi,
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
                                     options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasLikeSpecEmitterContext(eh, refEmitter, options) {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], reference.linkLabel.option(), Nil)

  override val factory: OasSpecEmitterFactory

  val typeDefMatcher: OasTypeDefStringValueMatcher = CommonOasTypeDefMatcher
}

class JsonSchemaEmitterContext(override val eh: ErrorHandler,
                               override val options: ShapeRenderOptions = ShapeRenderOptions(),
                               override val schemaVersion: JSONSchemaVersion)
    extends Oas2SpecEmitterContext(eh = eh, options = options) {
  override val typeDefMatcher: OasTypeDefStringValueMatcher = JsonSchemaTypeDefMatcher

  override val anyOfKey: String = "anyOf"

  override def schemasDeclarationsPath: String = "/definitions/"
}

object JsonSchemaEmitterContext {
  def apply(eh: ErrorHandler, options: ShapeRenderOptions): JsonSchemaEmitterContext =
    new JsonSchemaEmitterContext(eh, options, OAS20SchemaVersion("schema")(eh))
}

final case class CompactJsonSchemaEmitterContext(override val eh: ErrorHandler,
                                                 override val options: ShapeRenderOptions = ShapeRenderOptions(),
                                                 definitionsQueue: DefinitionsQueue = DefinitionsQueue(),
                                                 var forceEmission: Option[String] = None,
                                                 override val schemaVersion: JSONSchemaVersion)
    extends JsonSchemaEmitterContext(eh = eh, options = options, schemaVersion) {
  override val factory: OasSpecEmitterFactory = CompactJsonSchemaEmitterFactory()(this)
}

object CompactJsonSchemaEmitterContext {
  def apply(eh: ErrorHandler, options: ShapeRenderOptions): CompactJsonSchemaEmitterContext =
    CompactJsonSchemaEmitterContext(eh, options, schemaVersion = OAS20SchemaVersion("schema")(eh))
}

case class LabeledShape(label: String, shape: Shape)

case class DefinitionsQueue(pendingEmission: mutable.Queue[LabeledShape] = new mutable.Queue(),
                            queuedIdsWithLabel: mutable.Map[Id, Label] = mutable.Map[String, String]()) {

  val counter = new IdCounter()

  def enqueue(shape: Shape): String =
    queuedIdsWithLabel.getOrElse( // if the shape has already been queued the assigned label is returned
      shape.id, {
        val label        = createLabel(shape)
        val labeledShape = LabeledShape(label, shape)
        pendingEmission += labeledShape
        queuedIdsWithLabel += labeledShape.shape.id -> labeledShape.label
        labeledShape.label
      }
    )

  def createLabel(shape: Shape): String = {
    val name: String = normalizeName(shape.name.option())
    if (queuedIdsWithLabel.valuesIterator.contains(name)) counter.genId(name) else name
  }

  def labelOfShape(id: String): Option[String] = queuedIdsWithLabel.get(id)

  def nonEmpty(): Boolean     = pendingEmission.nonEmpty
  def dequeue(): LabeledShape = pendingEmission.dequeue
}

object DefinitionsEmissionHelper {
  type Label = String
  type Id    = String

  def normalizeName(name: Option[String]): String = name.filter(isValidName).getOrElse("default")

  val nameRegex: Regex                        = """^[a-zA-Z0-9\.\-_]+$""".r
  private def isValidName(s: String): Boolean = nameRegex.pattern.matcher(s).matches()
}

class Oas3SpecEmitterContext(eh: ErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasSpecEmitterContext(eh, refEmitter, options) {
  val schemaVersion: JSONSchemaVersion         = OAS30SchemaVersion("schema")(eh)
  override val factory: OasSpecEmitterFactory  = Oas3SpecEmitterFactory(this)
  override val vendor: Vendor                  = Oas30
  override def schemasDeclarationsPath: String = "/components/schemas/"
}

class Oas2SpecEmitterContext(eh: ErrorHandler,
                             refEmitter: RefEmitter = OasRefEmitter,
                             options: ShapeRenderOptions = ShapeRenderOptions())
    extends OasSpecEmitterContext(eh, refEmitter, options) {
  val schemaVersion: JSONSchemaVersion         = OAS20SchemaVersion("schema")(eh)
  override val factory: OasSpecEmitterFactory  = new Oas2SpecEmitterFactory(this)
  override val vendor: Vendor                  = Oas20
  override def schemasDeclarationsPath: String = "/definitions/"
}

object OasRefEmitter extends RefEmitter {

  override def ref(url: String, b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))
}
