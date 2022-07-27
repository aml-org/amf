package amf.apicontract.internal.spec.async.emitters.domain

import amf.apicontract.internal.spec.async.emitters.context.Async20SpecEmitterContext
import amf.apicontract.internal.spec.async.parser.domain.AsyncSchemaFormats
import amf.apicontract.internal.spec.oas.emitter.context.{OasLikeShapeEmitterContextAdapter, OasLikeSpecEmitterContext}
import amf.apicontract.internal.spec.raml.emitter
import amf.apicontract.internal.spec.spec.toRaml
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.{RAML10SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.oas.emitter.OasTypePartEmitter
import amf.shapes.internal.spec.raml.emitter.Raml10TypeEmitter
import org.yaml.model.YDocument.EntryBuilder

case class AsyncSchemaEmitter(
    key: String,
    shape: Shape,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    mediaType: Option[String] = None
)(implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val schemaVersion = AsyncSchemaFormats.getSchemaVersion(mediaType)(spec.eh)
    schemaVersion match {
      case RAML10SchemaVersion => emitAsRaml(b)
      case _                   => emitAsOas(b, schemaVersion)
    }
  }

  private def emitAsRaml(b: EntryBuilder): Unit = {
    val emitters =
      Raml10TypeEmitter(shape, ordering, references = references)(emitter.RamlShapeEmitterContextAdapter(toRaml(spec)))
        .entries()
    b.entry(
      key,
      _.obj(eb => emitters.foreach(_.emit(eb)))
    )
  }

  private def emitAsOas(b: EntryBuilder, schemaVersion: SchemaVersion): Unit = {
    b.entry(
      key,
      b => {
        val newCtx = new Async20SpecEmitterContext(spec.eh, config = spec.renderConfig, schemaVersion = schemaVersion)
        OasTypePartEmitter(shape, ordering, references = references)(OasLikeShapeEmitterContextAdapter(newCtx))
          .emit(b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
