package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, pos, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.internal.spec.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.domain.apicontract.models.Payload
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

case class OasContentPayloadsEmitter(payloads: Seq[Payload],
                                     ordering: SpecOrdering,
                                     references: Seq[BaseUnit],
                                     annotations: Annotations)(implicit spec: OasSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      annotations,
      b.obj { b =>
        val emitters = payloads.map(
          payload =>
            EntryPartEmitter(payload.mediaType.value(),
                             OasPayloadEmitter(payload, ordering, references),
                             YType.Str,
                             pos(payload.annotations)))
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(annotations)

}
