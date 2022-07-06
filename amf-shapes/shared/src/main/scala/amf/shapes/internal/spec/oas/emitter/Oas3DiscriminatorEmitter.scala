package amf.shapes.internal.spec.oas.emitter

import amf.core.internal.parser.domain.{FieldEntry, Fields}
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

case class Oas3DiscriminatorEmitter(found: FieldEntry, fs: Fields, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "discriminator",
      _.obj { b =>
        val result: ListBuffer[EntryEmitter] = ListBuffer()
        fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("propertyName", f))
        fs.entry(NodeShapeModel.DiscriminatorMapping).map(f => result += IriTemplateEmitter("mapping", f, ordering))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(found.value.annotations)
}
