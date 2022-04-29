package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.internal.render.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.SchemaDependencies
import amf.shapes.client.scala.model.domain.{Dependencies, NodeShape, PropertyDependencies, SchemaDependencies}
import amf.shapes.internal.domain.metamodel.NodeShapeModel.Dependencies
import amf.shapes.internal.domain.metamodel.{NodeShapeModel, PropertyDependenciesModel, SchemaDependenciesModel}
import amf.shapes.internal.spec.oas.emitter.OasTypeEmitter
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

trait TypeEmitterFactory {
  def emitterFor(shape: Shape): OasTypeEmitter
}

abstract class AbstractDependenciesEmitter(
    nodeShape: NodeShape,
    ordering: SpecOrdering,
    typeFactory: TypeEmitterFactory
) extends EntryEmitter {

  protected def schemaDependenciesEmitters: Seq[DependencyEmitter] = {
    nodeShape.fields
      .entry(NodeShapeModel.SchemaDependencies)
      .map { entry =>
        val dependencies = entry.arrayValues[SchemaDependencies]
        dependencies.map(dep => DependencyEmitter(dep, ordering, SchemaDependenciesEmitter(dep, ordering, typeFactory)))
      }
      .getOrElse(Seq())
  }

  protected def propertyDependenciesEmitters: Seq[DependencyEmitter] = {
    nodeShape.fields
      .entry(Dependencies)
      .map { entry =>
        val dependencies = entry.arrayValues[PropertyDependencies]
        dependencies.map(dep => DependencyEmitter(dep, ordering, PropertyDependenciesEmitter(dep, ordering)))
      }
      .getOrElse(Seq())
  }

  override def position(): Position =
    nodeShape.fields
      .entry(Dependencies)
      .orElse(nodeShape.fields.entry(NodeShapeModel.SchemaDependencies))
      .map(entry => pos(entry.value.annotations))
      .getOrElse(Position.ZERO)
}

case class Draft2019DependenciesEmitter(nodeShape: NodeShape, ordering: SpecOrdering, typeFactory: TypeEmitterFactory)
    extends AbstractDependenciesEmitter(nodeShape, ordering, typeFactory) {

  override def emit(b: EntryBuilder): Unit = {

    val propertyDependencies = propertyDependenciesEmitters
    val schemaDependencies   = schemaDependenciesEmitters

    if (propertyDependencies.nonEmpty) {
      b.entry(
        "dependentRequired",
        _.obj { b =>
          traverse(ordering.sorted(propertyDependencies), b)
        }
      )
    }

    if (schemaDependencies.nonEmpty) {
      b.entry(
        "dependentSchemas",
        _.obj { b =>
          traverse(ordering.sorted(schemaDependencies), b)
        }
      )
    }
  }
}

case class Draft4DependenciesEmitter(
    nodeShape: NodeShape,
    ordering: SpecOrdering,
    isRamlExtension: Boolean,
    typeFactory: TypeEmitterFactory
) extends AbstractDependenciesEmitter(nodeShape, ordering, typeFactory) {
  override def emit(b: EntryBuilder): Unit = {

    val key = if (isRamlExtension) "dependencies".asRamlAnnotation else "dependencies"

    val propertyEmitters   = propertyDependenciesEmitters
    val schemaDependencies = schemaDependenciesEmitters

    val result = propertyEmitters ++ schemaDependencies
    if (result.nonEmpty) {
      b.entry(
        key,
        _.obj { b =>
          traverse(ordering.sorted(result), b)
        }
      )
    }
  }
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

case class DependencyEmitter(dependency: Dependencies, ordering: SpecOrdering, emitter: PartEmitter)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      dependency.propertySource.value(),
      b => emitter.emit(b)
    )

  }

  override def position(): Position = pos(dependency.annotations)
}

case class PropertyDependenciesEmitter(dependency: Dependencies, ordering: SpecOrdering) extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    dependency.fields.entry(PropertyDependenciesModel.PropertyTarget).foreach { entry =>
      val targets = entry.array.scalars
      b.list { b =>
        traverse(ordering.sorted(targets.map(t => ScalarEmitter(AmfScalar(t)))), b)
      }
    }
  }

  override def position(): Position = pos(dependency.annotations)
}
