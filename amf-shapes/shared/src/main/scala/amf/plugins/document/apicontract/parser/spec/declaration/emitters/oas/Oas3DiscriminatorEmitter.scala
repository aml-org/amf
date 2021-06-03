package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.IriTemplateEmitter
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
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
