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
import org.yaml.model._

/**
  *
  */

case class Draft4ShapeDependenciesParser(shape: NodeShape,
                                         map: YMap,
                                         parentId: String,
                                         properties: Map[String, PropertyShape],
                                         version: SchemaVersion)(implicit ctx: OasLikeWebApiContext) {

  def parse(): Unit = {

    val mapEntries = getEntriesOfType(YType.Map)
    parseSchemaDependencies(mapEntries)

    val seqEntries = getEntriesOfType(YType.Seq)
    parsePropertyDependencies(seqEntries)
  }

  private def parsePropertyDependencies(seqEntries: IndexedSeq[YMapEntry]) = {
    val propertyDependencies = seqEntries.flatMap(e =>
      DependenciesParser(e, parentId, properties, PropertyDependencyParser(e.value, properties)).parse())
    if (propertyDependencies.nonEmpty)
      shape.set(NodeShapeModel.Dependencies,
        AmfArray(propertyDependencies, Annotations(VirtualObject())),
        Annotations(Inferred()))
  }

  private def parseSchemaDependencies(entries: Seq[YMapEntry]) = {
    val schemaDependencies = entries.flatMap(e =>
      DependenciesParser(e, parentId, properties, SchemaDependencyParser(e.value, version)).parse())
    if (schemaDependencies.nonEmpty)
      shape.set(NodeShapeModel.SchemaDependencies,
        AmfArray(schemaDependencies, Annotations(VirtualObject())),
        Annotations(Inferred()))
  }

  private def getEntriesOfType(tagType: YType) = map.entries.partition {
    case entry: YMapEntry => entry.value.tagType.equals(tagType)
    case _                => false
  }._1
}

case class Draft2019ShapeDependenciesParser(shape: NodeShape,
                                            map: YMap,
                                            parentId: String,
                                            properties: Map[String, PropertyShape],
                                            version: SchemaVersion)(implicit ctx: OasLikeWebApiContext) {
  def parse(): Unit = {
    map.key("dependentSchemas").foreach { entry =>
      val schemaDependencies = entry.value
        .as[YMap]
        .entries
        .flatMap(e => DependenciesParser(e, parentId, properties, SchemaDependencyParser(e.value, version)).parse())
      shape.set(NodeShapeModel.SchemaDependencies,
                AmfArray(schemaDependencies, Annotations(entry.value)),
                Annotations(entry))
    }

    map.key("dependentRequired").foreach { entry =>
      val propertyDependencies = entry.value
        .as[YMap]
        .entries
        .flatMap(e =>
          DependenciesParser(e, parentId, properties, PropertyDependencyParser(e.value, properties)).parse())
      shape
        .set(NodeShapeModel.Dependencies, AmfArray(propertyDependencies, Annotations(entry.value)), Annotations(entry))
    }
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
                              properties: Map[String, PropertyShape],
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
