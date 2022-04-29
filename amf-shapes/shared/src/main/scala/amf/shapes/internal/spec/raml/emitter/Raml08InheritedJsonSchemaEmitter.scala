package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class Raml08InheritedJsonSchemaEmitter(shape: Shape, ordering: SpecOrdering)(implicit
    spec: RamlShapeEmitterContext
) extends EntryEmitter
    with ExamplesEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val father =
      shape.inherits.collectFirst({ case s: Shape if s.annotations.contains(classOf[ParsedJSONSchema]) => s }).get
    val emitter = new EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        val emit: PartBuilder => Unit = Raml08TypePartEmitter(father, ordering, Nil).emit _
        b.entry("schema", emit)
      }

      override def position(): Position = pos(father.annotations)
    }

    val results = mutable.ListBuffer[EntryEmitter]()
    results += emitter
    shape match {
      case any: AnyShape if any.examples.nonEmpty => emitExamples(any, results, ordering, Nil)
      case _                                      => // ignore
    }

    results.foreach(_.emit(b))
  }

  override def position(): Position = pos(shape.annotations)
}
