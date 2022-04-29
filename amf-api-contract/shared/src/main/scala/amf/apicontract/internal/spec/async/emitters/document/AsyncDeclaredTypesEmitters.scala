package amf.apicontract.internal.spec.async.emitters.document

import amf.apicontract.internal.spec.async.emitters.context.Async20SpecEmitterContext
import amf.apicontract.internal.spec.oas.emitter
import amf.apicontract.internal.spec.oas.emitter.context.{OasLikeShapeEmitterContextAdapter, OasLikeSpecEmitterContext}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.common.emitter.OasDeclaredTypesEmitters

object AsyncDeclaredTypesEmitters {

  def obtainEmitter(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering, config: RenderConfiguration)(
      implicit spec: OasLikeSpecEmitterContext
  ): EntryEmitter = {
    val newCtx = new Async20SpecEmitterContext(spec.eh, schemaVersion = JSONSchemaDraft7SchemaVersion, config = config)
    OasDeclaredTypesEmitters(types, references, ordering)(OasLikeShapeEmitterContextAdapter(newCtx))
  }

}
