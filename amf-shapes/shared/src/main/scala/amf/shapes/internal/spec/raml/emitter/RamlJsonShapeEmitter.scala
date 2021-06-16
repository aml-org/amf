package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, pos, raw, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import org.yaml.model.YDocument.PartBuilder

import scala.collection.mutable

case class RamlJsonShapeEmitter(shape: AnyShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                typeKey: String = "type")(implicit spec: ShapeEmitterContext)
    extends PartEmitter
    with ExamplesEmitter {

  override def emit(b: PartBuilder): Unit = {
    shape.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(json) =>
        if (shape.examples.nonEmpty) {
          val results = mutable.ListBuffer[EntryEmitter]()
          emitExamples(shape, results, ordering, references)
          results += MapEntryEmitter(typeKey, json.rawText)
          b.obj(traverse(ordering.sorted(results), _))
        } else {
          raw(b, json.rawText)
        }
      case None => // Ignore
    }
  }

  override def position(): Position = {
    pos(shape.annotations)
  }
}
