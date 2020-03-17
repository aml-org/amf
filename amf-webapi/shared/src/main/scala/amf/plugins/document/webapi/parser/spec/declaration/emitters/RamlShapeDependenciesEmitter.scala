package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.domain.shapes.models.PropertyDependencies
import org.yaml.model.YDocument.EntryBuilder
import amf.core.utils.AmfStrings

import scala.collection.immutable.ListMap

case class RamlShapeDependenciesEmitter(f: FieldEntry, ordering: SpecOrdering, props: ListMap[String, PropertyShape])(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "dependencies".asRamlAnnotation,
      _.obj { b =>
        val result =
          f.array.values.map(v =>
            RamlPropertyDependenciesEmitter(v.asInstanceOf[PropertyDependencies], ordering, props))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
