package amf.plugins.document.apicontract.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters.{EntryPartEmitter, pos}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.parser.Position
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.domain.ExampleDataNodePartEmitter
import amf.plugins.document.apicontract.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.domain.shapes.models.Example
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
