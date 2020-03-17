package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.AmfScalar
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.domain.shapes.metamodel.PropertyDependenciesModel
import amf.plugins.domain.shapes.models.PropertyDependencies
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.immutable.ListMap

case class RamlPropertyDependenciesEmitter(
    property: PropertyDependencies,
    ordering: SpecOrdering,
    properties: ListMap[String, PropertyShape])(implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    properties
      .get(property.propertySource.value())
      .foreach(p => {
        b.entry(
          p.name.value(),
          b => {
            val targets = property.fields
              .entry(PropertyDependenciesModel.PropertyTarget)
              .map(f => {
                f.array.scalars.flatMap(iri =>
                  properties.get(iri.value.toString).map(p => AmfScalar(p.name.value(), iri.annotations)))
              })

            targets.foreach(target => {
              b.list { b =>
                traverse(ordering.sorted(target.map(t => ScalarEmitter(t))), b)
              }
            })
          }
        )
      })
  }

  override def position(): Position = pos(property.annotations) // TODO check this
}
