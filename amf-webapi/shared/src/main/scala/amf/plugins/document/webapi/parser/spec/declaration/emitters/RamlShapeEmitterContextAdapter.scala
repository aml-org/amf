package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.client.remod.amfcore.config.ShapeRenderOptions
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.FieldEntry
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.FacetsInstanceEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{CustomFacetsEmitter, SchemaVersion}
import amf.plugins.document.webapi.parser.spec.toOas
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YDocument

case class RamlShapeEmitterContextAdapter(spec: RamlSpecEmitterContext) extends RamlShapeEmitterContext {

  override def tagToReferenceEmitter(l: DomainElement with Linkable, refs: Seq[BaseUnit]): PartEmitter =
    spec.factory.tagToReferenceEmitter(l, refs)

  override def arrayEmitter(asOasExtension: String, f: FieldEntry, ordering: SpecOrdering): EntryEmitter =
    spec.arrayEmitter(asOasExtension, f, ordering)

  override def customFacetsEmitter(f: FieldEntry,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit]): CustomFacetsEmitter =
    spec.factory.customFacetsEmitter(f, ordering, references)

  override def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter =
    spec.factory.facetsInstanceEmitter(extension, ordering)

  override def annotationEmitter(e: DomainExtension, default: SpecOrdering): EntryEmitter =
    spec.factory.annotationEmitter(e, default)

  override def eh: ErrorHandler = spec.eh

  override def vendor: Vendor = spec.vendor

  override def ref(b: YDocument.PartBuilder, url: String): Unit = spec.ref(b, url)

  override def schemaVersion: SchemaVersion = spec.schemaVersion

  override def filterLocal(examples: Seq[Example]): Seq[Example] = spec.filterLocal(examples)

  override def options: ShapeRenderOptions = spec.options

  override def isOas3: Boolean = true

  override def isOasLike: Boolean = false

  override def isRaml: Boolean = true

  override def isJsonSchema: Boolean = false

  override def isAsync: Boolean = false

  override def toOasNext: OasLikeShapeEmitterContext = OasLikeShapeEmitterContextAdapter(toOas(spec))

  override def localReference(shape: Shape): PartEmitter = spec.localReference(shape)
}
