package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, sourceOr, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.OasSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, OasSchemaEmitter}
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class OasPayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      payload.annotations,
      b.obj { b =>
        val fs     = payload.fields
        val result = mutable.ListBuffer[EntryEmitter]()

        fs.entry(PayloadModel.Name).map(f => result += ValueEmitter("name", f))
        fs.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("mediaType", f))
        fs.entry(PayloadModel.Schema).map { f =>
          if (!f.value.value.annotations.contains(classOf[SynthesizedField])) {
            result += OasSchemaEmitter(f, ordering, references)
          }
        }

        result ++= AnnotationsEmitter(payload, ordering).emitters

        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(payload.annotations)
}

case class OasPayloadsEmitter(key: String, payloads: Seq[Payload], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.list(traverse(ordering.sorted(payloads.map(p => OasPayloadEmitter(p, ordering, references))), _))
    )
  }

  override def position(): Position = {
    val filtered = payloads
      .filter(p => p.annotations.find(classOf[LexicalInformation]).exists(!_.range.start.isZero))
    val result = filtered
      .foldLeft[Position](ZERO)(
        (pos, p) =>
          p.annotations
            .find(classOf[LexicalInformation])
            .map(_.range.start)
            .filter(newPos => pos.isZero || pos.lt(newPos))
            .getOrElse(pos))
    result
  }
}
