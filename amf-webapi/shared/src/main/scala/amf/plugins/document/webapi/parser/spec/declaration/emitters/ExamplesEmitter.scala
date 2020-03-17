package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.domain.{NamedMultipleExampleEmitter, SingleExampleEmitter}
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ExampleModel}
import amf.plugins.domain.shapes.models.{AnyShape, Example}

import scala.collection.mutable.ListBuffer

trait ExamplesEmitter {
  def emitExamples(shape: AnyShape,
                   results: ListBuffer[EntryEmitter],
                   ordering: SpecOrdering,
                   references: Seq[BaseUnit])(implicit spec: SpecEmitterContext): Unit = {
    shape.fields
      .entry(AnyShapeModel.Examples)
      .map(f => {
        val (anonymous, named) =
          spec
            .filterLocal(shape.examples)
            .partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)
        val examples = spec.filterLocal(f.array.values.collect({ case e: Example => e }))
        anonymous.headOption.foreach { a =>
          results += SingleExampleEmitter("example", a, ordering)
        }
        results += NamedMultipleExampleEmitter("examples",
                                               named ++ (if (anonymous.lengthCompare(1) > 0) examples.tail else None),
                                               ordering,
                                               references)
      })
  }
}
