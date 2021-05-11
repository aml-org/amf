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
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.async.AsyncSpecEmitterFactory
import amf.plugins.document.webapi.contexts.emitter.jsonschema.JsonSchemaEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.Oas3SpecEmitterFactory
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.FacetsInstanceEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{CustomFacetsEmitter, SchemaVersion}
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.{YDocument, YNode}

case class OasLikeShapeEmitterContextAdapter(spec: OasLikeSpecEmitterContext)
    extends AgnosticShapeEmitterContextAdapter(spec)
    with OasLikeShapeEmitterContext {

  override def recursiveShapeEmitter(recursive: RecursiveShape,
                                     ordering: SpecOrdering,
                                     schemaPath: Seq[(String, String)]): Emitter =
    spec.factory.recursiveShapeEmitter(recursive, ordering, schemaPath)

  override def schemasDeclarationsPath: String = spec.schemasDeclarationsPath

  override def customFacetsEmitter(f: FieldEntry,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit]): CustomFacetsEmitter =
    spec.factory.customFacetsEmitter(f, ordering, references)

  override def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter =
    spec.factory.facetsInstanceEmitter(extension, ordering)

  override def annotationEmitter(e: DomainExtension, default: SpecOrdering): EntryEmitter =
    spec.factory.annotationEmitter(e, default)

  override def anyOfKey: YNode = spec.anyOfKey

  override def typeEmitters(shape: Shape,
                            ordering: SpecOrdering,
                            ignored: Seq[Field],
                            references: Seq[BaseUnit],
                            pointer: Seq[String],
                            schemaPath: Seq[(String, String)]): Seq[Emitter] =
    spec.factory.typeEmitters(shape, ordering, ignored, references, pointer, schemaPath)
}
