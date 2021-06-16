package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.Payload
import amf.apicontract.internal.annotations.ParameterNameForPayload
import amf.apicontract.internal.metamodel.domain.PayloadModel
import amf.apicontract.internal.spec.oas.emitter.context.{
  Oas2SpecEmitterFactory,
  Oas3SpecEmitterFactory,
  OasLikeShapeEmitterContextAdapter,
  OasSpecEmitterContext
}
import amf.apicontract.internal.spec.oas.parser.domain.OasEncodingsEmitter
import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.spec.common.emitter.OasResponseExamplesEmitter
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.oas.emitter.OasSchemaEmitter
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class OasPayloadEmitter(payload: Payload, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PartEmitter {

  protected implicit val shapeCtx = OasLikeShapeEmitterContextAdapter(spec)

  override def emit(b: PartBuilder): Unit = {
    sourceOr(
      payload.annotations,
      b.obj { b =>
        val fs     = payload.fields
        val result = mutable.ListBuffer[EntryEmitter]()

        // OAS 3.0.0
        if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory]) {
          fs.entry(PayloadModel.Examples)
            .map(f => result += OasResponseExamplesEmitter("examples", f, ordering))

          fs.entry(PayloadModel.Encoding)
            .map(f => result += OasEncodingsEmitter("encoding", f, ordering, references))
        }

        // OAS 2.0
        if (spec.factory.isInstanceOf[Oas2SpecEmitterFactory]) {
          fs.entry(PayloadModel.Name)
            .map(f => {
              f.value.annotations.find(classOf[ParameterNameForPayload]) match {
                case Some(ann) =>
                  result += MapEntryEmitter("name", ann.paramName, position = ann.range.start)
                case _ =>
                  result += ValueEmitter("name", f)
              }
            })
          fs.entry(PayloadModel.MediaType).map(f => result += ValueEmitter("mediaType", f))
        }

        fs.entry(PayloadModel.Schema).map { f =>
          result += OasSchemaEmitter(f, ordering, references)
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
