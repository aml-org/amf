package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.domain.shapes.models.PropertyDependencies
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.immutable.ListMap

case class OasShapeDependenciesEmitter(f: FieldEntry,
                                       ordering: SpecOrdering,
                                       propertiesMap: ListMap[String, PropertyShape])
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    b.entry(
      "dependencies",
      _.obj { b =>
        val result = f.array.values.map(v =>
          OasPropertyDependenciesEmitter(v.asInstanceOf[PropertyDependencies], ordering, propertiesMap))
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
