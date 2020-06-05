package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.{MapEntryEmitter, pos, raw, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ExamplesEmitter
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.PartBuilder

import scala.collection.mutable

case class RamlJsonShapeEmitter(shape: AnyShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                typeKey: String = "type")(implicit spec: SpecEmitterContext)
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
