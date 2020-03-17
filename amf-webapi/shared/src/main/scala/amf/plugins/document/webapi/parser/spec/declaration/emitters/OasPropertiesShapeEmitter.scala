package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfElement
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasPropertiesShapeEmitter(f: FieldEntry,
                                     ordering: SpecOrdering,
                                     references: Seq[BaseUnit],
                                     pointer: Seq[String] = Nil,
                                     schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val properties = f.array.values.partition(_.asInstanceOf[PropertyShape].patternName.option().isDefined)

    // If properties not empty, emit it
    properties._2 match {
      case Nil  =>
      case some => emitProperties(some, "properties", b)
    }

    // If patternProperties not empty, emit it
    properties._1 match {
      case Nil  =>
      case some => emitProperties(some, "patternProperties", b)
    }
  }

  override def position(): Position = pos(f.value.annotations)

  private def emitProperties(properties: Seq[AmfElement], propertiesKey: String, b: EntryBuilder) {
    b.entry(
      propertiesKey,
      _.obj { b =>
        val result =
          properties.map(
            v =>
              OasPropertyShapeEmitter(v.asInstanceOf[PropertyShape],
                                      ordering,
                                      references,
                                      propertiesKey,
                                      pointer,
                                      schemaPath))
        traverse(ordering.sorted(result), b)
      }
    )
  }
}
