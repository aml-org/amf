package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.IdCounter
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, ExampleModel}
import amf.shapes.internal.spec.common.emitter.{
  SafeNamedMultipleExampleEmitter,
  ShapeEmitterContext,
  SingleExampleEmitter
}

import scala.collection.mutable.ListBuffer

trait ExamplesEmitter {
  def emitExamples(shape: AnyShape,
                   results: ListBuffer[EntryEmitter],
                   ordering: SpecOrdering,
                   references: Seq[BaseUnit])(implicit spec: ShapeEmitterContext): Unit = {
    shape.fields
      .entry(AnyShapeModel.Examples)
      .foreach(f => {
        val examples = shape.examples
        val (anonymous, named) =
          examples.partition(e => !e.fields.fieldsMeta().contains(ExampleModel.Name) && !e.isLink)
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
