package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.annotations.ParameterNameForPayload
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterFactory,
  Oas3SpecEmitterFactory,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasSchemaEmitter
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
          if (!f.value.annotations.contains(classOf[SynthesizedField])) {
            result += oas.OasSchemaEmitter(f, ordering, references)
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
