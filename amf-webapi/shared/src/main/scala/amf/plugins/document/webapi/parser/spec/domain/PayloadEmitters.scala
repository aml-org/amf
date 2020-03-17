package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.{emitters, _}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  Raml08TypePartEmitter,
  Raml10TypeEmitter,
  Raml10TypePartEmitter,
  RequiredShapeEmitter
}
import amf.plugins.document.webapi.parser.spec.raml.CommentEmitter
import amf.plugins.domain.shapes.models.{AnyShape, NodeShape}
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YMap, YNode, YType}

/**
  *
  */
case class Raml10PayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
      case Some(other) =>
        spec.eh.violation(ResolutionValidation,
                          other.id,
                          None,
                          "Cannot emit a non WebAPI Shape",
                          other.position(),
                          other.location())
      case None =>
        fs.entry(PayloadModel.MediaType)
          .foreach(mediaType => {
            b.complexEntry(
              ScalarEmitter(mediaType.scalar).emit(_),
              emitters
                .Raml10TypePartEmitter(null,
                                       ordering,
                                       Some(AnnotationsEmitter(payload, ordering)),
                                       references = references)
                .emit(_)
            )
          })
    }
  }

  override def position(): Position = pos(payload.annotations)
}

trait RamlPayloadsEmitter extends EntryEmitter {}

case class Raml08PayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlPayloadsEmitter {
  override def emit(b: EntryBuilder): Unit = {

    b.complexEntry(
      RawEmitter(key).emit(_),
      ob => {
        val payloads = f.array.values.collect({ case p: Payload => p })
        if (payloads.isEmpty) ob += YMap.empty
        else {
          val emitters = payloads.flatMap { p =>
            Raml08PayloadEmitter(p, ordering).emitters
          }
          emitters match {
            case Seq(pe: PartEmitter) => pe.emit(ob)
            case es if es.forall(_.isInstanceOf[EntryEmitter]) =>
              ob.obj(traverse(ordering.sorted(es.collect({ case e: EntryEmitter => e })), _))
            case other => throw new Exception(s"IllegalTypeDeclarations found: $other")
          }
        }
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class Raml08PayloadEmitter(payload: Payload, ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext) {

  def emitters: Seq[Emitter] = {
    payload.fields.entry(PayloadModel.MediaType) match {
      case Some(_) =>
        Seq(new EntryEmitter() {
          override def emit(b: EntryBuilder): Unit = {
            val emitters = typeEmitters
            if (emitters.nonEmpty) {
              b.entry(
                payload.mediaType.value(),
                p => {
                  emitters match {
                    case Seq(pe: PartEmitter) => pe.emit(p)
                    case es if es.forall(_.isInstanceOf[EntryEmitter]) =>
                      p.obj(traverse(ordering.sorted(es.collect { case e: EntryEmitter => e }), _))
                    case other =>
                      spec.eh.violation(ResolutionValidation,
                                        payload.id,
                                        None,
                                        s"IllegalTypeDeclarations found: $other",
                                        payload.position(),
                                        payload.location())
                  }
                }
              )
            } else {
              b.entry(
                payload.mediaType.value(),
                p => raw(p, "", YType.Null)
              )
            }
          }

          override def position(): Position = pos(payload.annotations)
        })
      case None => typeEmitters
    }
  }

  val typeEmitters: Seq[Emitter] = {
    Option(payload.schema) match {
      case Some(s: Shape) if s.annotations.contains(classOf[SynthesizedField]) => Nil
      case Some(node: NodeShape) if !node.isLink && node.annotations.find(classOf[ParsedJSONSchema]).isEmpty =>
        Seq(Raml08FormPropertiesEmitter(node, ordering))
      case Some(anyShape: AnyShape) =>
        Raml08TypePartEmitter(anyShape, ordering, Seq()).emitters
      case Some(other) =>
        Seq(CommentEmitter(other, s"Cannot emit schema ${other.getClass.toString} in raml 08 body request"))
      case None =>
        Seq(new PartEmitter() {
          override def emit(b: PartBuilder): Unit = b.+=(YNode(YMap.empty)) // ignore

          override def position(): Position = Position.ZERO
        })
    }
  }
}

case class Raml08FormPropertiesEmitter(nodeShape: NodeShape, ordering: SpecOrdering)(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "formParameters",
      builder => {
        builder.obj(ob => {
          nodeShape.properties.foreach {
            p =>
              p.range match {
                case anyShape: AnyShape =>
                  ob.entry(
                    p.name.value(),
                    pb => {
                      emitters.Raml08TypePartEmitter(anyShape, ordering, None, Seq(), Seq()).emitter match {
                        case Left(prop) => prop.emit(pb)
                        case Right(entries) =>
                          val additionalEmitters: Seq[EntryEmitter] =
                            RequiredShapeEmitter(shape = p.range, p.fields.entry(PropertyShapeModel.MinCount))
                              .emitter() match {
                              case Some(emitter) => Seq(emitter)
                              case None          => Nil
                            }
                          pb.obj(traverse(ordering.sorted(entries ++ additionalEmitters), _))
                      }
                    }
                  )

                case other =>
                  ob.entry(p.name.value(),
                           CommentEmitter(
                             other,
                             s"Cannot emit property ${other.getClass.toString} in raml 08 form properties").emit(_))
              }

          }
        })
      }
    )
  }

  override def position(): Position = pos(nodeShape.annotations)
}

case class Raml10PayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
    implicit spec: RamlSpecEmitterContext) {
  def emitters(): Seq[Emitter] = {
    if (payload.fields.entry(PayloadModel.MediaType).isDefined) {
      Seq(Raml10PayloadEmitter(payload, ordering, references = references))
    } else {
      Option(payload.schema) match {
        case Some(shape: AnyShape) => Raml10TypeEmitter(shape, ordering, references = references).emitters()
        case Some(other) =>
          spec.eh.violation(ResolutionValidation,
                            other.id,
                            None,
                            "Cannot emit a non WebAPI shape",
                            payload.position(),
                            payload.location())
          Nil
        case _ => Nil // ignore
      }
    }
  }
}
