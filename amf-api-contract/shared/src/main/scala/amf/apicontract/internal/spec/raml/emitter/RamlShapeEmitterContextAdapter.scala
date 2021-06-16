package amf.apicontract.internal.spec.raml.emitter

import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeShapeEmitterContextAdapter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.apicontract.internal.spec.spec.toOas
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.client.scala.domain.models.AnyShape
import amf.shapes.internal.spec.common.emitter.annotations.{AnnotationsEmitter, FacetsInstanceEmitter}
import amf.shapes.internal.spec.common.emitter.{
  CustomFacetsEmitter,
  OasLikeShapeEmitterContext,
  RamlShapeEmitterContext
}
import amf.shapes.internal.spec.raml.emitter.RamlTypePartEmitter
import org.yaml.model.YNode

case class RamlShapeEmitterContextAdapter(spec: RamlSpecEmitterContext)
    extends AgnosticShapeEmitterContextAdapter(spec)
    with RamlShapeEmitterContext {

  override def typesEmitter
    : (AnyShape, SpecOrdering, Option[AnnotationsEmitter], Seq[Field], Seq[BaseUnit]) => RamlTypePartEmitter =
    spec.factory.typesEmitter

  override def typesKey: YNode = spec.factory.typesKey

  override def customFacetsEmitter(f: FieldEntry,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit]): CustomFacetsEmitter =
    spec.factory.customFacetsEmitter(f, ordering, references)

  override def facetsInstanceEmitter(extension: ShapeExtension, ordering: SpecOrdering): FacetsInstanceEmitter =
    spec.factory.facetsInstanceEmitter(extension, ordering)

  override def annotationEmitter(e: DomainExtension, default: SpecOrdering): EntryEmitter =
    spec.factory.annotationEmitter(e, default)

  override def toOasNext: OasLikeShapeEmitterContext = OasLikeShapeEmitterContextAdapter(toOas(spec))

  override def localReference(shape: Shape): PartEmitter = spec.localReference(shape)
}
