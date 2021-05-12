package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{Emitter, EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.model.domain.{DomainElement, RecursiveShape, Shape}
import amf.core.parser.FieldEntry
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.CustomFacetsEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.FacetsInstanceEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasRecursiveShapeEmitter
import org.yaml.model.YNode

import scala.util.matching.Regex

case class OasLikeShapeEmitterContextAdapter(spec: OasLikeSpecEmitterContext)
    extends AgnosticShapeEmitterContextAdapter(spec)
    with OasLikeShapeEmitterContext {

  override def nameRegex: Regex = spec.nameRegex

//  override def recursiveShapeEmitter(recursive: RecursiveShape, ordering: SpecOrdering, schemaPath: Seq[(String, String)]): EntryEmitter = OasRecursiveShapeEmitter(recursive, ordering, schemaPath)

  override protected implicit val shapeCtx: OasLikeShapeEmitterContext = this

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

  override def compactEmission: Boolean = spec.options.isWithCompactedEmission
}
