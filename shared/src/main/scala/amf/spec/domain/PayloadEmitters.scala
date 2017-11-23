package amf.spec.domain

import amf.framework.model.document.BaseUnit
import amf.domain.FieldEntry
import amf.framework.parser.Position
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import amf.spec.common.BaseEmitters.ScalarEmitter
import amf.spec.declaration.{AnnotationsEmitter, RamlTypeEmitter, RamlTypePartEmitter}
import amf.spec.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import org.yaml.model.YDocument.EntryBuilder
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext

/**
  *
  */
case class RamlPayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val fs = payload.fields
    fs.entry(PayloadModel.MediaType)
      .foreach(mediaType => {
        b.complexEntry(
          ScalarEmitter(mediaType.scalar).emit(_),
          RamlTypePartEmitter(payload.schema,
                              ordering,
                              Some(AnnotationsEmitter(payload, ordering)),
                              references = references).emit(_)
        )
      })
  }

  override def position(): Position = pos(payload.annotations)
}

case class RamlPayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value.annotations, {
        payloads(f, ordering, references) match {
          case Seq(p: PartEmitter) => b.entry(key, b => p.emit(b))
          case es if es.forall(_.isInstanceOf[EntryEmitter]) =>
            b.entry(key, _.obj(traverse(es.collect { case e: EntryEmitter => e }, _)))
          case other => throw new Exception(s"IllegalTypeDeclarations found: $other")
        }
      }
    )
  }

  private def payloads(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[Emitter] = {
    ordering.sorted(
      f.array.values.flatMap(e => RamlPayloads(e.asInstanceOf[Payload], ordering, references = references).emitters()))
  }

  override def position(): Position = pos(f.value.annotations)
}

case class RamlPayloads(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    if (payload.fields.entry(PayloadModel.MediaType).isDefined) {
      Seq(RamlPayloadEmitter(payload, ordering, references = references))
    } else {
      RamlTypeEmitter(payload.schema, ordering, references = references).emitters()
    }
  }
}
