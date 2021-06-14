package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter}
import amf.plugins.document.apicontract.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.apicontract.contexts.emitter.oas.{DefinitionsQueue, OasSpecEmitterContext}
import amf.plugins.document.apicontract.parser.spec.declaration.CustomFacetsEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.FacetsInstanceEmitter
import org.yaml.model.YNode

import scala.util.matching.Regex

case class OasLikeShapeEmitterContextAdapter(spec: OasLikeSpecEmitterContext)
    extends AgnosticShapeEmitterContextAdapter(spec)
    with OasLikeShapeEmitterContext {

  override def nameRegex: Regex = spec.nameRegex

//  override def recursiveShapeEmitter(recursive: RecursiveShape, ordering: SpecOrdering, schemaPath: Seq[(String, String)]): EntryEmitter = OasRecursiveShapeEmitter(recursive, ordering, schemaPath)

  override def forceEmission: Option[String] = spec match {
    case oasCtx: OasSpecEmitterContext => oasCtx.forceEmission
    case _                             => super.forceEmission
  }

  override def setForceEmission(id: Option[String]): Unit = spec match {
    case oasCtx: OasSpecEmitterContext => oasCtx.setForceEmission(id)
    case _                             => super.setForceEmission(id)
  }

  override def removeForceEmission: Unit = spec match {
    case oasCtx: OasSpecEmitterContext => oasCtx.removeForceEmission
    case _                             => super.removeForceEmission
  }

  override val definitionsQueue: DefinitionsQueue = spec match {
    case oasCtx: OasSpecEmitterContext => oasCtx.definitionsQueue
    case _                             => DefinitionsQueue()(this)
  }
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
}
