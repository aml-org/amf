package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.spec.common.emitter.{ExampleDataNodePartEmitter, ShapeEmitterContext}
import amf.shapes.internal.spec.oas.emitter.OasLikeExampleEmitters
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

case class Draft6ExamplesEmitter(examples: Seq[Example], ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends OasLikeExampleEmitters
    with EntryEmitter {

  private def entryEmitter: EntryEmitter =
    EntryPartEmitter("examples",
                     ExamplesArrayPartEmitter(examples, ordering),
                     position = examples.headOption.map(h => pos(h.annotations)).getOrElse(Position.ZERO))

  override def emit(b: YDocument.EntryBuilder): Unit = entryEmitter.emit(b)

  override def emitters(): ListBuffer[EntryEmitter] = ListBuffer(entryEmitter)

  override def position(): Position = examples.headOption.map(ex => pos(ex.annotations)).getOrElse(Position.ZERO)
}

case class ExamplesArrayPartEmitter(examples: Seq[Example], ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit = {
    b.list { listBuilder =>
      examples.foreach(ex => ExampleDataNodePartEmitter(ex, ordering).emit(listBuilder))
    }
  }
  override def position(): Position = examples.headOption.map(ex => pos(ex.annotations)).getOrElse(Position.ZERO)
}
