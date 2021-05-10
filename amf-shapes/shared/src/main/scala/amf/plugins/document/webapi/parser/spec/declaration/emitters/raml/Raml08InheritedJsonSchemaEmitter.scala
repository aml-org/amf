package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{ExamplesEmitter, ShapeEmitterContext}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable

case class Raml08InheritedJsonSchemaEmitter(shape: Shape, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter
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
