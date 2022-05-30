package amf.shapes.internal.spec.common.emitter

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.client.scala.model.domain.{CustomizableElement, DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Spec
import amf.core.internal.render.BaseEmitters.MultipleValuesArrayEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.annotations.{
  AnnotationsEmitter,
  FacetsInstanceEmitter,
  OasAnnotationEmitter,
  OasFacetsInstanceEmitter
}
import amf.shapes.internal.spec.common.{JSONSchemaDraft201909SchemaVersion, JSONSchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.contexts.DeclarationEmissionDecorator
import amf.shapes.internal.spec.contexts.emitter.oas.{CompactableEmissionContext, OasCompactEmitterFactory}
import amf.shapes.internal.spec.oas.emitter.compact.CompactOasRecursiveShapeEmitter
import amf.shapes.internal.spec.raml.emitter.{Raml10TypePartEmitter, RamlTypePartEmitter}
import org.yaml.model.{YDocument, YNode}

import scala.util.matching.Regex

trait SpecAwareEmitterContext {
  def isOas3: Boolean
  def isOasLike: Boolean
  def isRaml: Boolean
  def isJsonSchema: Boolean
  def isAsync: Boolean
}

trait RamlShapeEmitterContext extends ShapeEmitterContext {
  def typesEmitter
      : (AnyShape, SpecOrdering, Option[AnnotationsEmitter], Seq[Field], Seq[BaseUnit]) => RamlTypePartEmitter
  def typesKey: YNode

  def localReference(shape: Shape): PartEmitter
  def toOasNext: OasLikeShapeEmitterContext
}

object JsonSchemaDeclarationsPath {
  def apply(schemaVersion: SchemaVersion): String = schemaVersion match {
    case jsonVersion: JSONSchemaVersion =>
      if (jsonVersion < JSONSchemaDraft201909SchemaVersion) "/definitions/"
      else "/$defs/"
    case _ => "/definitions/"
  }
}

/** InlinedJsonSchemaShape context is used when emitting a single shape in a non compacted form. This implies having to
  * use compact declaredTypesEmitter and recursiveShapeEmitter emitters to handle shapes that have RecursiveShapes,
  * emitting their fixpoint target to the schemas definitions facet dynamically.
  */
class InlineJsonSchemaShapeEmitterContext(
    eh: AMFErrorHandler,
    schemaVersion: SchemaVersion,
    config: RenderConfiguration
) extends JsonSchemaShapeEmitterContext(eh, schemaVersion, config) {
  override def recursiveShapeEmitter(
      shape: RecursiveShape,
      ordering: SpecOrdering,
      schemaPath: Seq[(String, String)]
  ): EntryEmitter = {
    new CompactOasRecursiveShapeEmitter(shape, ordering, schemaPath)
  }

  override def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter = {
    CompactOasTypesEmitters.apply
  }
}

object JsonSchemaShapeEmitterContext {
  def apply(eh: AMFErrorHandler, schemaVersion: SchemaVersion, config: RenderConfiguration) =
    new JsonSchemaShapeEmitterContext(eh, schemaVersion, config)
}

class JsonSchemaShapeEmitterContext(
    val eh: AMFErrorHandler,
    val schemaVersion: SchemaVersion,
    val config: RenderConfiguration
) extends OasLikeShapeEmitterContext {

  override def options: RenderOptions = config.renderOptions

  override def nameRegex: Regex = """^[a-zA-Z0-9.\-_]+$""".r

  override def schemasDeclarationsPath: String = JsonSchemaDeclarationsPath(schemaVersion)

  override def anyOfKey: YNode = "anyOf"

  override def tagToReferenceEmitter(linkable: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter =
    OasShapeReferenceEmitter(linkable)

  override def arrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter =
    MultipleValuesArrayEmitter(key, f, ordering)

  override def customFacetsEmitter(
      f: FieldEntry,
      ordering: SpecOrdering,
      references: Seq[BaseUnit]
  ): CustomFacetsEmitter =
    OasCustomFacetsEmitter(f, ordering, references)

  override def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter =
    OasFacetsInstanceEmitter(extension, ordering)

  override def annotationEmitter(parent: CustomizableElement, e: DomainExtension, default: SpecOrdering): EntryEmitter =
    OasAnnotationEmitter(parent, e, default)

  override def spec: Spec = Spec.JSONSCHEMA

  override def ref(b: YDocument.PartBuilder, url: String): Unit = OasRefEmitter.ref(url, b)

  override protected implicit val shapeCtx: OasLikeShapeEmitterContext = this

  override def isOas3: Boolean = false

  override def isOasLike: Boolean = true

  override def isRaml: Boolean = false

  override def isJsonSchema: Boolean = true

  override def isAsync: Boolean = false
}

trait OasLikeShapeEmitterContext
    extends ShapeEmitterContext
    with CompactableEmissionContext
    with OasCompactEmitterFactory {

  def recursiveShapeEmitter(
      recursive: RecursiveShape,
      ordering: SpecOrdering,
      schemaPath: Seq[(String, String)]
  ): Emitter

  def schemasDeclarationsPath: String
  def typeEmitters(
      shape: Shape,
      ordering: SpecOrdering,
      ignored: Seq[Field],
      references: Seq[BaseUnit],
      pointer: Seq[String],
      schemaPath: Seq[(String, String)]
  ): Seq[Emitter]
  def anyOfKey: YNode
}

trait ShapeEmitterContext extends SpecAwareEmitterContext with DeclarationEmissionDecorator {

  def config: RenderConfiguration

  def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter

  def arrayEmitter(asOasExtension: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter

  def customFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): CustomFacetsEmitter

  def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter

  def annotationEmitter(parent: CustomizableElement, e: DomainExtension, default: SpecOrdering): EntryEmitter

  def eh: AMFErrorHandler

  def spec: Spec

  def ref(b: YDocument.PartBuilder, url: String): Unit

  def schemaVersion: SchemaVersion

  def options: RenderOptions
}
