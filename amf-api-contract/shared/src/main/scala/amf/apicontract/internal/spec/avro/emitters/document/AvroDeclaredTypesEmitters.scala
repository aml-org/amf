package amf.apicontract.internal.spec.avro.emitters.document

import amf.apicontract.internal.spec.avro.emitters.context.AvroSpecEmitterContext
import amf.apicontract.internal.spec.oas.emitter.context.{OasLikeShapeEmitterContextAdapter, OasLikeSpecEmitterContext}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.OasDeclaredTypesEmitters

object AvroDeclaredTypesEmitters {
  def obtainEmitter(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering, config: RenderConfiguration)(
      implicit spec: OasLikeSpecEmitterContext
  ): EntryEmitter = {
    val newCtx = new AvroSpecEmitterContext(spec.eh, config = config)
    OasDeclaredTypesEmitters(types, references, ordering)(OasLikeShapeEmitterContextAdapter(newCtx))
  }
}
