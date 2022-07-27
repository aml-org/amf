package amf.shapes.internal.spec.oas.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.AmfElement
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasPropertiesShapeEmitter(
    f: FieldEntry,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeShapeEmitterContext)
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
          properties.map(v =>
            OasPropertyShapeEmitter(
              v.asInstanceOf[PropertyShape],
              ordering,
              references,
              propertiesKey,
              pointer,
              schemaPath
            )
          )
        traverse(ordering.sorted(result), b)
      }
    )
  }
}
