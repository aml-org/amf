package amf.apicontract.internal.spec.raml.emitter.domain

import amf.apicontract.internal.spec.raml.emitter.RamlShapeEmitterContextAdapter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter

case class RamlCustomFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends CustomFacetsEmitter(f, ordering, references) {

  private implicit val shapeCtx = RamlShapeEmitterContextAdapter(spec)
  override val key: String      = "facets"

  override def shapeEmitter: (PropertyShape, SpecOrdering, Seq[BaseUnit]) => EntryEmitter =
    RamlPropertyShapeEmitter.apply
}
