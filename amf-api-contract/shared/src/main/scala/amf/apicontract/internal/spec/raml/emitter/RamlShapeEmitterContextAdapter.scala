package amf.apicontract.internal.spec.raml.emitter

import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeShapeEmitterContextAdapter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.apicontract.internal.spec.spec.toOas
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{CustomizableElement, Shape}
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.annotations.{AnnotationsEmitter, FacetsInstanceEmitter}
import amf.shapes.internal.spec.common.emitter.{
  CustomFacetsEmitter,
  OasLikeShapeEmitterContext,
  RamlShapeEmitterContext
}
import amf.shapes.internal.spec.raml.emitter.RamlTypePartEmitter
import org.yaml.model.YNode

case class RamlShapeEmitterContextAdapter(specCtx: RamlSpecEmitterContext)
    extends AgnosticShapeEmitterContextAdapter(specCtx)
    with RamlShapeEmitterContext {

  override def config: RenderConfiguration = specCtx.renderConfig

  override def typesEmitter
    : (AnyShape, SpecOrdering, Option[AnnotationsEmitter], Seq[Field], Seq[BaseUnit]) => RamlTypePartEmitter =
    specCtx.factory.typesEmitter

  override def typesKey: YNode = specCtx.factory.typesKey

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

  override def toOasNext: OasLikeShapeEmitterContext = OasLikeShapeEmitterContextAdapter(toOas(specCtx))

  override def localReference(shape: Shape): PartEmitter = specCtx.localReference(shape)
}
