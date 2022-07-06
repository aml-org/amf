package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.ShapeExtension
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.remote.{Oas, Raml}
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.raml.parser.{ClosedRamlTypeShape, TypeInfo}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  MissingRequiredUserDefinedFacet,
  UnableToParseShapeExtensions,
  UserDefinedFacetMatchesAncestorsTypeFacets,
  UserDefinedFacetMatchesBuiltInFacets
}
import org.yaml.model.YMap
import amf.core.internal.utils._

case class ShapeExtensionParser(
    shape: Shape,
    map: YMap,
    ctx: ShapeParserContext,
    typeInfo: TypeInfo,
    overrideSyntax: Option[String] = None
) {
  def parse(): Unit = {
    val inheritedDefinitions =
      shape.collectCustomShapePropertyDefinitions(onlyInherited = true).flatMap(_.values).distinct
    val directlyInherited = shape.effectiveInherits.flatMap(_.customShapePropertyDefinitions)
    inheritedDefinitions.foreach { shapeExtensionDefinition =>
      val extensionKey = ctx.spec match {
        case _: Raml => shapeExtensionDefinition.name.value() // TODO check this.
        case _: Oas  => s"facet-${shapeExtensionDefinition.name.value()}".asOasExtension
        case _ =>
          ctx.eh.violation(
            UnableToParseShapeExtensions,
            shape,
            s"Cannot parse shape extension for spec ${ctx.spec}",
            map.location
          )
          shapeExtensionDefinition.name.value()
      }
      map.key(extensionKey) match {
        case Some(entry) =>
          val dataNode =
            DataNodeParser(entry.value)(ctx).parse()
          val extension = ShapeExtension(entry)
            .withDefinedBy(shapeExtensionDefinition)
            .withExtension(dataNode)
          shape.add(ShapeModel.CustomShapeProperties, extension)
        case None if directlyInherited.contains(shapeExtensionDefinition) =>
          if (shapeExtensionDefinition.minCount.option().exists(_ > 0)) {
            ctx.eh.violation(
              MissingRequiredUserDefinedFacet,
              shape,
              s"Missing required facet '$extensionKey'",
              map.location
            )
          }
        case _ =>
      }
    }
    if (!shape.inherits.exists(s => s.isUnresolved)) { // only validate shapes when the father its resolved, to avoid close shape over custom annotations
      val syntax = overrideSyntax match {
        case Some("anyShape") | Some("shape") => shape.ramlSyntaxKey
        case Some(other)                      => other
        case None                             => shape.ramlSyntaxKey
      }

      val extensionsNames = inheritedDefinitions.flatMap(_.name.option())
      val m               = YMap(map.entries.filter(e => !extensionsNames.contains(e.key.value.toString)), "")
      ClosedRamlTypeShape.eval(shape, m, syntax, typeInfo)(ctx)
      validateCustomFacetDefinitions(syntax)
    }

    // todo: filter map.entries by extension key and call close shape by instance of for the rest?
  }

  def validateCustomFacetDefinitions(syntax: String): Unit = {
    ctx.syntax.nodes
      .get(syntax)
      .foreach(builtInFacets => {
        val definedCustomFacets   = shape.customShapePropertyDefinitions.flatMap(_.name.option())
        val inheritedCustomFacets = shape.collectCustomShapePropertyDefinitions(onlyInherited = true).flatten.map(_._1)
        definedCustomFacets.toSet
          .intersect(builtInFacets)
          .foreach(name =>
            ctx.eh.violation(
              UserDefinedFacetMatchesBuiltInFacets,
              shape,
              s"Custom defined facet '$name' matches built-in type facets",
              map.location
            )
          )
        definedCustomFacets
          .intersect(inheritedCustomFacets)
          .foreach(name =>
            ctx.eh.violation(
              UserDefinedFacetMatchesAncestorsTypeFacets,
              shape,
              s"Custom defined facet '$name' matches custom facet from inherited type",
              map.location
            )
          )
      })
  }

}
