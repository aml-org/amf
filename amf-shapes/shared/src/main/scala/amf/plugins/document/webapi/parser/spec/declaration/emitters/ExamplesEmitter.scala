package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Annotations
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.parser.spec.domain.{SafeNamedMultipleExampleEmitter, SingleExampleEmitter}
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, ExampleModel}
import amf.plugins.domain.shapes.models.{AnyShape, Example}

import scala.collection.mutable.ListBuffer

trait ExamplesEmitter {
  def emitExamples(shape: AnyShape,
                   results: ListBuffer[EntryEmitter],
                   ordering: SpecOrdering,
                   references: Seq[BaseUnit])(implicit spec: ShapeEmitterContext): Unit = {
    shape.fields
      .entry(AnyShapeModel.Examples)
      .foreach(f => {
        val (anonymous, named) =
          spec
            .filterLocal(shape.examples)
            .partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)
        val examples = spec.filterLocal(f.array.values.collect({ case e: Example => e }))
        if (examples.size == 1 && named.isEmpty) {
          anonymous.headOption.foreach { a =>
            results += SingleExampleEmitter("example", a, ordering)
          }
        } else {
          val idCounter = new IdCounter()
          anonymous.foreach(e => e.withName(idCounter.genId("generated"), Annotations.synthesized()))
          results += SafeNamedMultipleExampleEmitter("examples", examples, ordering, references)
        }
      })
  }
}
