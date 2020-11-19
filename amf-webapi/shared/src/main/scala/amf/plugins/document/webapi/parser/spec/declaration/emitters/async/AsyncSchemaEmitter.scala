package amf.plugins.document.webapi.parser.spec.declaration.emitters.async

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.async.Async20SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.async.parser.AsyncSchemaFormats
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypePartEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.Raml10TypeEmitter
import amf.plugins.document.webapi.parser.spec.declaration.{SchemaVersion, RAML10SchemaVersion}
import amf.plugins.document.webapi.parser.spec.toRaml
import org.yaml.model.YDocument.EntryBuilder

case class AsyncSchemaEmitter(key: String,
                              shape: Shape,
                              ordering: SpecOrdering,
                              references: Seq[BaseUnit],
                              mediaType: Option[String] = None)(implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val schemaVersion = AsyncSchemaFormats.getSchemaVersion(mediaType)(spec.eh)
    schemaVersion match {
      case RAML10SchemaVersion() => emitAsRaml(b)
      case _                     => emitAsOas(b, schemaVersion)
    }
  }

  private def emitAsRaml(b: EntryBuilder): Unit = {
    val emitters = Raml10TypeEmitter(shape, ordering, references = references)(toRaml(spec)).entries()
    b.entry(
      key,
      _.obj(eb => emitters.foreach(_.emit(eb)))
    )
  }

  private def emitAsOas(b: EntryBuilder, schemaVersion: SchemaVersion): Unit = {
    b.entry(
      key,
      b => {
        val newCtx = new Async20SpecEmitterContext(spec.eh, schemaVersion = schemaVersion)
        OasTypePartEmitter(shape, ordering, references = references)(newCtx).emit(b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
