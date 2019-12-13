package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{SpecOrdering, PartEmitter}
import amf.core.model.document.BaseUnit
import amf.core.parser.{Annotations, Position}
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

case class OasContentPayloadsEmitter(payloads: Seq[Payload], ordering: SpecOrdering, references: Seq[BaseUnit], annotations: Annotations)(
  implicit spec: OasSpecEmitterContext)
  extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      annotations,
      b.obj { b =>
        val emitters = payloads.map(payload => EntryPartEmitter(payload.mediaType.value(), OasPayloadEmitter(payload, ordering, references), YType.Str, pos(payload.annotations)))
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(annotations)

}
