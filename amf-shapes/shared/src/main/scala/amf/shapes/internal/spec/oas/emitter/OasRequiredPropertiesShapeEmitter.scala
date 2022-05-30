package amf.shapes.internal.spec.oas.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.{Annotations, FieldEntry}
import amf.core.internal.render.BaseEmitters.{TextScalarEmitter, pos}
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasRequiredPropertiesShapeEmitter(f: FieldEntry, references: Seq[BaseUnit])(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val requiredProperties = f.array.values.filter {
      case property: PropertyShape if property.patternName.isNullOrEmpty => property.minCount.value() > 0
      case _                                                             => false
    }
    if (requiredProperties.nonEmpty) {
      b.entry(
        "required",
        _.list { b =>
          requiredProperties.foreach { case property: PropertyShape =>
            TextScalarEmitter(property.name.value(), Annotations()).emit(b)
          }
        }
      )
    }
  }

  override def position(): Position = pos(f.value.annotations)
}
