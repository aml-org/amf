package amf.shapes.internal.spec.raml.parser

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import amf.shapes.internal.spec.raml.emitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class RamlTypeEntryEmitter(key: String, shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        val emitters = emitter.Raml10TypeEmitter(shape, ordering, references = references).entries()
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
