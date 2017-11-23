package amf.spec.declaration

import amf.domain.FieldEntry
import amf.framework.model.document.BaseUnit
import amf.framework.parser.Position
import amf.plugins.domain.shapes.models.PropertyShape
import amf.remote.{Oas, Raml}
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import amf.spec.{EntryEmitter, SpecOrdering}
import org.yaml.model.YDocument.EntryBuilder


case class CustomFacetsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
  implicit spec: SpecEmitterContext)
  extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val label = spec.vendor match {
      case Raml => "facets"
      case Oas  => "x-facets"
      case _    => throw new Exception(s"Custom facets not supported for vendor ${spec.vendor}")
    }


    b.entry(
      label,
      _.obj { b =>
        val result = f.array.values.map { v =>
          spec vendor match {
            case Raml => RamlPropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering, references)
            case Oas  => OasPropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering, references)
            case _    => throw new Exception(s"Unsupported vendor for shape facets ${spec.vendor}")
          }

        }
        traverse(ordering.sorted(result), b)
      }
    )

  }

  override def position(): Position = pos(f.value.annotations)
}