package amf.plugins.document.webapi.parser.spec.common

import amf.core.emitter.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.Position
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeEmitter
import amf.plugins.domain.shapes.metamodel.NodeShapeModel.Dependencies
import amf.plugins.domain.shapes.metamodel.{NodeShapeModel, PropertyDependenciesModel, SchemaDependenciesModel}
import amf.plugins.domain.shapes.models.{Dependencies, NodeShape, PropertyDependencies, SchemaDependencies}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.immutable.ListMap

trait TypeEmitterFactory {
  def emitterFor(shape: Shape): OasTypeEmitter
}

case class ShapeDependenciesEmitter(nodeShape: NodeShape,
                                    ordering: SpecOrdering,
                                    propertiesMap: ListMap[String, PropertyShape],
                                    isRamlExtension: Boolean,
                                    typeFactory: TypeEmitterFactory)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    val key = if (isRamlExtension) "dependencies".asRamlAnnotation else "dependencies"

    val propertyEmitters = nodeShape.fields
      .entry(Dependencies)
      .map { entry =>
        val dependencies = entry.arrayValues[PropertyDependencies]
        dependencies.map(dep =>
          DependencyEmitter(dep, ordering, propertiesMap, PropertyDependenciesEmitter(dep, ordering, propertiesMap)))
      }
      .getOrElse(Seq())

    val schemaDependencies = nodeShape.fields
      .entry(NodeShapeModel.SchemaDependencies)
      .map { entry =>
        val dependencies = entry.arrayValues[SchemaDependencies]
        dependencies.map(dep =>
          DependencyEmitter(dep, ordering, propertiesMap, SchemaDependenciesEmitter(dep, ordering, typeFactory)))
      }
      .getOrElse(Seq())

    b.entry(
      key,
      _.obj { b =>
        val result = propertyEmitters ++ schemaDependencies
        traverse(ordering.sorted(result), b)
      }
    )
  }

  override def position(): Position =
    nodeShape.fields
      .entry(Dependencies)
      .orElse(nodeShape.fields.entry(NodeShapeModel.SchemaDependencies))
      .map(entry => pos(entry.value.annotations))
      .getOrElse(Position.ZERO)
}

case class SchemaDependenciesEmitter(dependency: Dependencies, ordering: SpecOrdering, typeFactory: TypeEmitterFactory)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    dependency.fields.entry(SchemaDependenciesModel.SchemaTarget).foreach { entry =>
      val shape    = entry.element.asInstanceOf[Shape]
      val emitters = typeFactory.emitterFor(shape).emitters().collect { case e: EntryEmitter => e }
      b.obj { obj =>
        emitters.foreach(_.emit(obj))
      }
    }
  }

  override def position(): Position = pos(dependency.annotations)
}

case class DependencyEmitter(dependency: Dependencies,
                             ordering: SpecOrdering,
                             properties: ListMap[String, PropertyShape],
                             emitter: PartEmitter)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    properties
      .get(dependency.propertySource.value())
      .foreach { p =>
        b.entry(
          p.name.value(),
          b => emitter.emit(b)
        )
      }
  }

  override def position(): Position = pos(dependency.annotations)
}

case class PropertyDependenciesEmitter(dependency: Dependencies,
                                       ordering: SpecOrdering,
                                       properties: ListMap[String, PropertyShape])
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    dependency.fields.entry(PropertyDependenciesModel.PropertyTarget).foreach { entry =>
      val targets = entry.array.scalars.flatMap(iri =>
        properties.get(iri.value.toString).map(p => AmfScalar(p.name.value(), iri.annotations)))
      b.list { b =>
        traverse(ordering.sorted(targets.map(t => ScalarEmitter(AmfScalar(t)))), b)
      }
    }
  }

  override def position(): Position = pos(dependency.annotations)
}
