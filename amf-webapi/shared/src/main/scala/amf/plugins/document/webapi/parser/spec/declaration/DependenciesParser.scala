package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.VirtualObject
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.annotations.Inferred
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{SingleArrayNode, YMapEntryLike}
import amf.plugins.domain.shapes.metamodel.{DependenciesModel, NodeShapeModel, PropertyDependenciesModel, SchemaDependenciesModel}
import amf.plugins.domain.shapes.models.{Dependencies, NodeShape, PropertyDependencies, SchemaDependencies}
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YType}

import scala.collection.mutable

/**
  *
  */

case class ShapeDependenciesParser(shape: NodeShape,
                                   map: YMap,
                                   parentId: String,
                                   properties: mutable.LinkedHashMap[String, PropertyShape],
                                   version: SchemaVersion)(implicit ctx: OasLikeWebApiContext) {
  def parse(): Unit = {
    val (mapEntries, _) = map.entries.partition {
      case entry: YMapEntry => entry.value.tagType.equals(YType.Map)
      case _                => false
    }
    val schemaDependencies = mapEntries.flatMap(e => DependenciesParser(e, parentId, properties, SchemaDependencyParser(e.value, version)).parse())
    if (schemaDependencies.nonEmpty) shape.set(NodeShapeModel.SchemaDependencies, AmfArray(schemaDependencies, Annotations(VirtualObject())), Annotations(Inferred()))
    val (seqEntries, _) = map.entries.partition {
      case entry : YMapEntry => entry.value.tagType.equals(YType.Seq)
      case _ => false
    }
    val propertyDependencies = seqEntries.flatMap(e => DependenciesParser(e, parentId, properties, PropertyDependencyParser(e.value, properties.toMap)).parse())
    if (propertyDependencies.nonEmpty) shape.set(NodeShapeModel.Dependencies, AmfArray(propertyDependencies, Annotations(VirtualObject())), Annotations(Inferred()))
  }
}

trait SpecializedDependencyParser {
  def create(entry: YMapEntry): Dependencies
  def parse(dependency: Dependencies): Dependencies
}

case class SchemaDependencyParser(node: YNode, version: SchemaVersion)(implicit ctx: OasLikeWebApiContext)
    extends SpecializedDependencyParser {

  override def create(entry: YMapEntry): Dependencies = SchemaDependencies(entry)

  override def parse(dependency: Dependencies): Dependencies = {
    val optionalShape =
      OasTypeParser(YMapEntryLike(node.as[YMap]), "schema", shape => shape.adopted(dependency.id), version).parse()
    optionalShape.foreach { s =>
      dependency.set(SchemaDependenciesModel.SchemaTarget, s, Annotations(node))
    }
    dependency
  }
}

case class PropertyDependencyParser(node: YNode, properties: Map[String, PropertyShape])(
    implicit ctx: OasLikeWebApiContext)
    extends SpecializedDependencyParser {

  override def create(entry: YMapEntry): Dependencies = PropertyDependencies(entry)

  override def parse(dependency: Dependencies): Dependencies = {
    dependency.set(PropertyDependenciesModel.PropertyTarget, AmfArray(targets()), Annotations(node))
  }

  private def targets(): Seq[AmfScalar] = {
    SingleArrayNode(node)
      .text()
      .scalars
      .flatMap(v => properties.get(v.value.toString).map(p => AmfScalar(p.id, v.annotations)))
  }
}

case class DependenciesParser(entry: YMapEntry,
                              parentId: String,
                              properties: mutable.LinkedHashMap[String, PropertyShape],
                              parser: SpecializedDependencyParser)(implicit ctx: OasLikeWebApiContext) {
  def parse(): Option[Dependencies] = {

    properties
      .get(dependencyKey)
      .map { p =>
        val dependency = parser.create(entry)
        dependency.set(DependenciesModel.PropertySource, AmfScalar(p.id), Annotations(entry.key))
        dependency.withId(parentId + dependency.componentId)
        parser.parse(dependency)
      }
  }

  private def dependencyKey = entry.key.as[YScalar].text
}
