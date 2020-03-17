package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration
import org.yaml.model.YDocument.EntryBuilder

case class OasEntryShapeEmitter(key: String,
                                shape: Shape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        val emitters =
          declaration.emitters
            .OasTypeEmitter(shape, ordering, references = references, pointer = pointer :+ key, schemaPath = schemaPath)
            .entries()
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
