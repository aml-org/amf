package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.{DomainExtension, ShapeExtension}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.CustomFacetsEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.{
  AnnotationsEmitter,
  FacetsInstanceEmitter
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml.RamlTypePartEmitter
import amf.plugins.document.apicontract.parser.spec.toOas
import amf.plugins.domain.shapes.models.AnyShape
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
