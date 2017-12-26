package amf.plugins.document.webapi.parser.spec.domain

import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.model.document.BaseUnit
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.document.webapi.parser.spec.declaration.{
  AnnotationsEmitter,
  Raml08TypePartEmitter,
  Raml10TypeEmitter,
  Raml10TypePartEmitter
}
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YMap, YNode}

/**
  *
  */
case class Raml10PayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
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
              Raml10TypePartEmitter(shape,
                                    ordering,
                                    Some(AnnotationsEmitter(payload, ordering)),
                                    references = references).emit(_)
            )
          })
      case Some(_) => throw new Exception("Cannot emit a non WebAPI Shape")
      case None =>
        fs.entry(PayloadModel.MediaType)
          .foreach(mediaType => {
            b.complexEntry(
              ScalarEmitter(mediaType.scalar).emit(_),
              Raml10TypePartEmitter(null,
                                    ordering,
                                    Some(AnnotationsEmitter(payload, ordering)),
                                    references = references).emit(_)
            )
          })
    }
  }

  override def position(): Position = pos(payload.annotations)
}

trait RamlPayloadsEmitter extends EntryEmitter {}

case class Raml08PayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlPayloadsEmitter {
  override def emit(b: EntryBuilder): Unit = {

    b.complexEntry(
      RawEmitter(key).emit(_),
      ob => {
        val payloads = f.array.values.collect({ case p: Payload => p })
        val emitters = payloads.map { p =>
          Raml08PayloadEmitter(p, ordering)
        }
        ob.obj(traverse(ordering.sorted(emitters), _))
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class Raml08PayloadEmitter(payload: Payload, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      payload.mediaType,
      p => {
        Option(payload.schema) match {

          case Some(node: NodeShape) if !node.isLink && node.annotations.find(classOf[ParsedJSONSchema]).isEmpty =>
            p.obj(Raml08FormPropertiesEmitter(node, ordering).emit)
          case Some(anyShape: AnyShape) =>
            Raml08TypePartEmitter(anyShape, ordering, Seq()).emit(_)
          case Some(other) => throw new Exception(s"Cannot emit schema $other in raml 08 body request")
          case None        => p.+=(YNode(YMap.empty)) // ignore
        }
      }
    )
  }

  override def position(): Position = pos(payload.annotations)
}

case class Raml08FormPropertiesEmitter(nodeShape: NodeShape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "formParameters",
      builder => {
        builder.obj(ob => {
          nodeShape.properties.foreach { p =>
            p.range match {
              case anyShape: AnyShape =>
                ob.entry(p.name, Raml08TypePartEmitter(anyShape, ordering, None, Seq(), Seq()).emit(_))
              case other => throw new Exception(s"Cannot emit property $other in raml 08 form properties")
            }

          }
        })
      }
    )
  }

  override def position(): Position = pos(nodeShape.annotations)
}

case class Raml10PayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends RamlPayloadsEmitter {
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
    ordering.sorted(f.array.values.flatMap(e =>
      Raml10Payloads(e.asInstanceOf[Payload], ordering, references = references).emitters()))
  }

  override def position(): Position = pos(f.value.annotations)
}

case class Raml10Payloads(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    if (payload.fields.entry(PayloadModel.MediaType).isDefined) {
      Seq(Raml10PayloadEmitter(payload, ordering, references = references))
    } else {
      Option(payload.schema) match {
        case Some(shape: AnyShape) => Raml10TypeEmitter(shape, ordering, references = references).emitters()
        case Some(_)               => throw new Exception("Cannot emit a non WebAPI shape")
        case _                     => Nil // ignore
      }
    }
  }
}
