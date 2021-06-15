package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.contexts.emitter.OasLikeSpecEmitterContext
import amf.shapes.internal.spec.contexts.emitter.async.Async20SpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContextAdapter

object AsyncDeclaredTypesEmitters {

  def obtainEmitter(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)(
      implicit spec: OasLikeSpecEmitterContext): EntryEmitter = {
    val newCtx = new Async20SpecEmitterContext(spec.eh, schemaVersion = JSONSchemaDraft7SchemaVersion)
    OasDeclaredTypesEmitters(types, references, ordering)(OasLikeShapeEmitterContextAdapter(newCtx))
  }

}
