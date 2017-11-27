package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.model.document.BaseUnit
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.parser.spec.declaration.{AnnotationsEmitter, RamlTypeEmitter, RamlTypePartEmitter}
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.YDocument.EntryBuilder

/**
  *
  */
case class RamlPayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val fs = payload.fields
    Option(payload.schema) match {
      case Some(shape: AnyShape) =>
        fs.entry(PayloadModel.MediaType)
          .foreach(mediaType => {
            b.complexEntry(
              ScalarEmitter(mediaType.scalar).emit(_),
              RamlTypePartEmitter(shape,
                ordering,
                Some(AnnotationsEmitter(payload, ordering)),
                references = references).emit(_)
            )
          })
      case Some(_) => throw new Exception("Cannot emit a non WebAPI Shape")
      case None    =>
        fs.entry(PayloadModel.MediaType)
          .foreach(mediaType => {
            b.complexEntry(
              ScalarEmitter(mediaType.scalar).emit(_),
              RamlTypePartEmitter(null,
                ordering,
                Some(AnnotationsEmitter(payload, ordering)),
                references = references).emit(_)
            )
          })
    }
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
      Option(payload.schema) match {
        case Some(shape: AnyShape) => RamlTypeEmitter(shape, ordering, references = references).emitters()
        case Some(_)               => throw new Exception("Cannot emit a non WebAPI shape")
        case _                     => Nil// ignore
      }
    }
  }
}
