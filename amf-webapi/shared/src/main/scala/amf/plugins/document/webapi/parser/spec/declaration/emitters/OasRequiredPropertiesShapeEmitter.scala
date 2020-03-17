package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{TextScalarEmitter, pos}
import amf.core.emitter.EntryEmitter
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasRequiredPropertiesShapeEmitter(f: FieldEntry, references: Seq[BaseUnit])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val requiredProperties = f.array.values.filter {
      case property: PropertyShape if property.patternName.isNullOrEmpty => property.minCount.value() > 0
      case _                                                             => false
    }
    if (requiredProperties.nonEmpty) {
      b.entry(
        "required",
        _.list { b =>
          requiredProperties.foreach {
            case property: PropertyShape =>
              TextScalarEmitter(property.name.value(), Annotations()).emit(b)
          }
        }
      )
    }
  }

  override def position(): Position = pos(f.value.annotations)
}
