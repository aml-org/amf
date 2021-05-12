package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, RecursiveShape, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.DeclarationEmissionDecorator
import amf.plugins.document.webapi.contexts.emitter.oas.{CompactableEmissionContext, OasCompactEmitterFactory}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.{
  AnnotationsEmitter,
  FacetsInstanceEmitter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.RamlTypePartEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{CustomFacetsEmitter, SchemaVersion}
import amf.plugins.domain.shapes.models.{AnyShape, Example}
import org.yaml.model.{YDocument, YNode}

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

trait OasLikeShapeEmitterContext
    extends ShapeEmitterContext
    with CompactableEmissionContext
    with OasCompactEmitterFactory {

  def compactEmission: Boolean

  def recursiveShapeEmitter(recursive: RecursiveShape,
                            ordering: SpecOrdering,
                            schemaPath: Seq[(String, String)]): Emitter

  def schemasDeclarationsPath: String
  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field],
                   references: Seq[BaseUnit],
                   pointer: Seq[String],
                   schemaPath: Seq[(String, String)]): Seq[Emitter]
  def anyOfKey: YNode

  override def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] =
    super[CompactableEmissionContext].filterLocal(elements)
}

trait ShapeEmitterContext extends SpecAwareEmitterContext with DeclarationEmissionDecorator {

  def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter

  def arrayEmitter(asOasExtension: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter

  def customFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): CustomFacetsEmitter

  def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter

  def annotationEmitter(e: DomainExtension, default: SpecOrdering): EntryEmitter

  def eh: ErrorHandler

  def vendor: Vendor

  def ref(b: YDocument.PartBuilder, url: String): Unit

  def schemaVersion: SchemaVersion

  def options: ShapeRenderOptions
}
