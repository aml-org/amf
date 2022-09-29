package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.Payload
import amf.apicontract.internal.metamodel.domain.PayloadModel
import amf.apicontract.internal.spec.raml.emitter
import amf.apicontract.internal.spec.raml.emitter.RamlShapeEmitterContextAdapter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.internal.spec.common.emitter.CommentEmitter
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.raml.emitter.{
  Raml08TypePartEmitter,
  Raml10TypeEmitter,
  Raml10TypePartEmitter,
  RamlRequiredShapeEmitter
}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YMap, YNode, YType}

/** */
case class Raml10PayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends EntryEmitter {

  protected implicit val shapeCtx: RamlShapeEmitterContextAdapter = RamlShapeEmitterContextAdapter(spec)

  override def emit(b: EntryBuilder): Unit = {
    val fs = payload.fields
    Option(payload.schema) match {
      case Some(shape: AnyShape) =>
        fs.entry(PayloadModel.MediaType)
          .foreach(mediaType => {
            b.complexEntry(
              ScalarEmitter(mediaType.scalar).emit(_),
              Raml10TypePartEmitter(
                shape,
                ordering,
                Some(AnnotationsEmitter(payload, ordering)),
                references = references
              )
                .emit(_)
            )
          })
      case Some(other) =>
        spec.eh.violation(
          TransformationValidation,
          other.id,
          None,
          "Cannot emit a non WebAPI Shape",
          other.position(),
          other.location()
        )
      case None =>
        fs.entry(PayloadModel.MediaType)
          .foreach(mediaType => {
            b.complexEntry(
              ScalarEmitter(mediaType.scalar).emit(_),
              Raml10TypePartEmitter(
                null,
                ordering,
                Some(AnnotationsEmitter(payload, ordering)),
                references = references
              )
                .emit(_)
            )
          })
    }
  }

  override def position(): Position = pos(payload.annotations)
}

trait RamlPayloadsEmitter extends EntryEmitter {}

case class Raml08PayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends RamlPayloadsEmitter {
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
          Raml08PayloadsEmitter.processEmitters(ob, emitters, ordering)
        }
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

object Raml08PayloadsEmitter {
  def processEmitters(b: PartBuilder, emitters: Seq[Emitter], ordering: SpecOrdering): Unit = {
    emitters match {
      case Seq(pe: PartEmitter) => pe.emit(b)
      case es if es.forall(_.isInstanceOf[EntryEmitter]) =>
        b.obj(traverse(ordering.sorted(es.collect({ case e: EntryEmitter => e })), _))
      case other => throw new Exception(s"IllegalTypeDeclarations found: $other")
    }
  }
}

case class Raml10PayloadPartEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    val emitters = Raml08PayloadEmitter(payload, ordering).emitters
    Raml08PayloadsEmitter.processEmitters(b, emitters, ordering)
  }

  override def position(): Position = pos(payload.annotations)
}

case class Raml08PayloadEmitter(payload: Payload, ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext) {

  protected implicit val shapeCtx: RamlShapeEmitterContextAdapter = emitter.RamlShapeEmitterContextAdapter(spec)

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
                      spec.eh.violation(
                        TransformationValidation,
                        payload.id,
                        None,
                        s"IllegalTypeDeclarations found: $other",
                        payload.position(),
                        payload.location()
                      )
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
      case Some(s: Shape) if s.annotations.isSynthesized => Nil
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

case class Raml08FormPropertiesEmitter(nodeShape: NodeShape, ordering: SpecOrdering)(implicit
    spec: RamlSpecEmitterContext
) extends EntryEmitter {

  protected implicit val shapeCtx: RamlShapeEmitterContextAdapter = emitter.RamlShapeEmitterContextAdapter(spec)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "formParameters",
      builder => {
        builder.obj(ob => {
          nodeShape.properties.foreach { p =>
            p.range match {
              case anyShape: AnyShape =>
                ob.entry(
                  p.name.value(),
                  pb => {
                    Raml08TypePartEmitter(anyShape, ordering, None, Seq(), Seq()).emitter match {
                      case Left(prop) => prop.emit(pb)
                      case Right(entries) =>
                        val additionalEmitters: Seq[EntryEmitter] =
                          RamlRequiredShapeEmitter(shape = p.range, p.fields.entry(PropertyShapeModel.MinCount))
                            .emitter() match {
                            case Some(emitter) => Seq(emitter)
                            case None          => Nil
                          }
                        pb.obj(traverse(ordering.sorted(entries ++ additionalEmitters), _))
                    }
                  }
                )

              case other =>
                val prop = if (other != null) other.getClass.toString else "null"
                ob.entry(
                  p.name.value(),
                  CommentEmitter(other, s"Cannot emit property $prop in raml 08 form properties").emit(_)
                )
            }

          }
        })
      }
    )
  }

  override def position(): Position = pos(nodeShape.annotations)
}

case class Raml10PayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) extends RamlPayloadsEmitter {
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
      f.array.values.flatMap(e => Raml10Payloads(e.asInstanceOf[Payload], ordering, references = references).emitters())
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class Raml10Payloads(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlSpecEmitterContext
) {

  protected implicit val shapeCtx: RamlShapeEmitterContextAdapter = emitter.RamlShapeEmitterContextAdapter(spec)

  def emitters(): Seq[Emitter] = {
    if (payload.fields.entry(PayloadModel.MediaType).isDefined) {
      Seq(Raml10PayloadEmitter(payload, ordering, references = references))
    } else {
      Option(payload.schema) match {
        case Some(shape: AnyShape) => Raml10TypeEmitter(shape, ordering, references = references).emitters()
        case Some(other) =>
          spec.eh.violation(
            TransformationValidation,
            other.id,
            None,
            "Cannot emit a non WebAPI shape",
            payload.position(),
            payload.location()
          )
          Nil
        case _ => Nil // ignore
      }
    }
  }
}
