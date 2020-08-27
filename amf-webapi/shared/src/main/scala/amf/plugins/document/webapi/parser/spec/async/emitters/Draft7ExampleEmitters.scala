package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters.{EntryPartEmitter, pos}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.DataNodeEmitter
import amf.plugins.document.webapi.parser.spec.domain.ExampleDataNodePartEmitter
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

object Draft7ExampleEmitters {

  def apply(examples: Seq[Example], ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: OasLikeSpecEmitterContext): Draft7ExampleEmitters = {
    new Draft7ExampleEmitters(examples, ordering, references)
  }
}

class Draft7ExampleEmitters(examples: Seq[Example], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasLikeSpecEmitterContext)
    extends OasLikeExampleEmitters {
  override def emitters(): ListBuffer[EntryEmitter] = {
    ListBuffer(
      EntryPartEmitter("examples",
                       ExamplesArrayPartEmitter(examples, ordering, references),
                       position = examples.headOption.map(h => pos(h.annotations)).getOrElse(Position.ZERO))
    )
  }
}

case class ExamplesArrayPartEmitter(examples: Seq[Example], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasLikeSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit = {
    b.list { listBuilder =>
      examples.foreach(ex => ExampleDataNodePartEmitter(ex, ordering)(spec).emit(listBuilder))
    }
  }
  override def position(): Position = examples.headOption.map(ex => pos(ex.annotations)).getOrElse(Position.ZERO)
}
