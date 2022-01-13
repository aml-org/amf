package amf.apicontract.internal.spec.oas.emitter.context

import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{CustomizableElement, Shape}
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter}
import amf.shapes.internal.spec.common.emitter.annotations.FacetsInstanceEmitter
import amf.shapes.internal.spec.common.emitter.{CustomFacetsEmitter, OasLikeShapeEmitterContext}
import amf.shapes.internal.spec.contexts.emitter.oas.DefinitionsQueue
import org.yaml.model.YNode

import scala.util.matching.Regex

case class OasLikeShapeEmitterContextAdapter(specCtx: OasLikeSpecEmitterContext)
    extends AgnosticShapeEmitterContextAdapter(specCtx)
    with OasLikeShapeEmitterContext {

  override def config: RenderConfiguration = specCtx.renderConfig

  override def nameRegex: Regex = specCtx.nameRegex

  override def forceEmission: Option[String] = specCtx match {
    case oasCtx: OasSpecEmitterContext => oasCtx.forceEmission
    case _                             => super.forceEmission
  }

  override def setForceEmission(id: Option[String]): Unit = specCtx match {
    case oasCtx: OasSpecEmitterContext => oasCtx.setForceEmission(id)
    case _                             => super.setForceEmission(id)
  }

  override def removeForceEmission: Unit = specCtx match {
    case oasCtx: OasSpecEmitterContext => oasCtx.removeForceEmission()
    case _                             => super.removeForceEmission()
  }

  override val definitionsQueue: DefinitionsQueue = specCtx match {
    case oasCtx: OasSpecEmitterContext => oasCtx.definitionsQueue
    case _                             => DefinitionsQueue()(this)
  }
  override protected implicit val shapeCtx: OasLikeShapeEmitterContext = this

  override def schemasDeclarationsPath: String = specCtx.schemasDeclarationsPath

  override def customFacetsEmitter(f: FieldEntry,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit]): CustomFacetsEmitter =
    specCtx.factory.customFacetsEmitter(f, ordering, references)

  override def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter =
    specCtx.factory.facetsInstanceEmitter(extension, ordering)

  override def annotationEmitter(parent: CustomizableElement,
                                 e: DomainExtension,
                                 default: SpecOrdering): EntryEmitter =
    specCtx.factory.annotationEmitter(parent, e, default)

  override def anyOfKey: YNode = specCtx.anyOfKey

  override def typeEmitters(shape: Shape,
                            ordering: SpecOrdering,
                            ignored: Seq[Field],
                            references: Seq[BaseUnit],
                            pointer: Seq[String],
                            schemaPath: Seq[(String, String)]): Seq[Emitter] =
    specCtx.factory.typeEmitters(shape, ordering, ignored, references, pointer, schemaPath)
}
