package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasEntryShapeEmitter(key: String,
                                shape: Shape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        val emitters =
          OasTypeEmitter(shape, ordering, references = references, pointer = pointer :+ key, schemaPath = schemaPath)
            .entries()
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
