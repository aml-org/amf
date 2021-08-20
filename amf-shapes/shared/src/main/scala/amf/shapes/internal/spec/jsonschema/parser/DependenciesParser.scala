package amf.shapes.internal.spec.jsonschema.parser

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.SchemaDependencies
import amf.shapes.client.scala.model.domain.{Dependencies, NodeShape, PropertyDependencies}
import amf.shapes.internal.domain.metamodel.{
  DependenciesModel,
  NodeShapeModel,
  PropertyDependenciesModel,
  SchemaDependenciesModel
}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.{SingleArrayNode, YMapEntryLike}
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model._

/**
  *
  */
case class Draft4ShapeDependenciesParser(shape: NodeShape, map: YMap, parentId: String, version: SchemaVersion)(
    implicit ctx: ShapeParserContext) {

  def parse(): Unit = {

    val mapEntries = getEntriesOfType(YType.Map)
    parseSchemaDependencies(mapEntries)

    val seqEntries = getEntriesOfType(YType.Seq)
    parsePropertyDependencies(seqEntries)
  }

  private def parsePropertyDependencies(seqEntries: IndexedSeq[YMapEntry]) = {
    val propertyDependencies =
      seqEntries.map(e => DependenciesParser(e, parentId, PropertyDependencyParser(e.value)).parse())
    if (propertyDependencies.nonEmpty)
      shape.setWithoutId(NodeShapeModel.Dependencies,
                AmfArray(propertyDependencies, Annotations.virtual()),
                Annotations.inferred())
  }

  private def parseSchemaDependencies(entries: Seq[YMapEntry]) = {
    val schemaDependencies =
      entries.map(e => DependenciesParser(e, parentId, SchemaDependencyParser(e.value, version)).parse())
    if (schemaDependencies.nonEmpty)
      shape.setWithoutId(NodeShapeModel.SchemaDependencies,
                AmfArray(schemaDependencies, Annotations.virtual()),
                Annotations.inferred())
  }

  private def getEntriesOfType(tagType: YType) =
    map.entries.partition {
      case entry: YMapEntry => entry.value.tagType.equals(tagType)
      case _                => false
    }._1
}

case class Draft2019ShapeDependenciesParser(shape: NodeShape, map: YMap, parentId: String, version: SchemaVersion)(
    implicit ctx: ShapeParserContext) {
  def parse(): Unit = {
    map.key("dependentSchemas").foreach { entry =>
      val schemaDependencies = entry.value
        .as[YMap]
        .entries
        .map(e => DependenciesParser(e, parentId, SchemaDependencyParser(e.value, version)).parse())
      shape.setWithoutId(NodeShapeModel.SchemaDependencies,
                AmfArray(schemaDependencies, Annotations(entry.value)),
                Annotations(entry))
    }

    map.key("dependentRequired").foreach { entry =>
      val propertyDependencies = entry.value
        .as[YMap]
        .entries
        .map(e => DependenciesParser(e, parentId, PropertyDependencyParser(e.value)).parse())
      shape
        .setWithoutId(NodeShapeModel.Dependencies, AmfArray(propertyDependencies, Annotations(entry.value)), Annotations(entry))
    }
  }
}

trait SpecializedDependencyParser {
  def create(entry: YMapEntry): Dependencies
  def parse(dependency: Dependencies): Dependencies
}

case class SchemaDependencyParser(node: YNode, version: SchemaVersion)(implicit ctx: ShapeParserContext)
    extends SpecializedDependencyParser {

  override def create(entry: YMapEntry): Dependencies = SchemaDependencies(entry)

  override def parse(dependency: Dependencies): Dependencies = {
    val optionalShape =
      OasTypeParser(YMapEntryLike(node.as[YMap]), "schema", shape => Unit, version).parse()
    optionalShape.foreach { s =>
      dependency.setWithoutId(SchemaDependenciesModel.SchemaTarget, s, Annotations(node))
    }
    dependency
  }
}

case class PropertyDependencyParser(node: YNode)(implicit ctx: ShapeParserContext)
    extends SpecializedDependencyParser {

  override def create(entry: YMapEntry): Dependencies = PropertyDependencies(entry)

  override def parse(dependency: Dependencies): Dependencies = {
    dependency.setWithoutId(PropertyDependenciesModel.PropertyTarget, AmfArray(targets()), Annotations(node))
  }

  private def targets(): Seq[AmfScalar] = {
    SingleArrayNode(node)
      .text()
      .scalars
  }
}

case class DependenciesParser(entry: YMapEntry, parentId: String, parser: SpecializedDependencyParser)(
    implicit ctx: ShapeParserContext) {
  def parse(): Dependencies = {
    val dependency = parser.create(entry)
    dependency.setWithoutId(DependenciesModel.PropertySource, AmfScalar(dependencyKey), Annotations(entry.key))
    dependency.withId(parentId + dependency.componentId)
    parser.parse(dependency)
  }

  private def dependencyKey = entry.key.as[YScalar].text
}
