package amf.plugins.document.apicontract.parser.spec.common

import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.model.domain.extensions.ShapeExtension
import amf.core.parser.YMapOps
import amf.core.remote.{Oas, Raml}
import amf.core.utils.AmfStrings
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.document.apicontract.parser.spec.declaration.TypeInfo
import amf.validations.ShapeParserSideValidations.{
  MissingRequiredUserDefinedFacet,
  UnableToParseShapeExtensions,
  UserDefinedFacetMatchesAncestorsTypeFacets,
  UserDefinedFacetMatchesBuiltInFacets
}
import org.yaml.model.YMap

case class ShapeExtensionParser(shape: Shape,
                                map: YMap,
                                ctx: ShapeParserContext,
                                typeInfo: TypeInfo,
                                overrideSyntax: Option[String] = None) {
  def parse(): Unit = {
    val inheritedDefinitions =
      shape.collectCustomShapePropertyDefinitions(onlyInherited = true).flatMap(_.values).distinct
    val directlyInherited = shape.effectiveInherits.flatMap(_.customShapePropertyDefinitions)
    inheritedDefinitions.foreach { shapeExtensionDefinition =>
      val extensionKey = ctx.vendor match {
        case _: Raml => shapeExtensionDefinition.name.value() // TODO check this.
        case _: Oas  => s"facet-${shapeExtensionDefinition.name.value()}".asOasExtension
        case _ =>
          ctx.eh.violation(UnableToParseShapeExtensions,
                           shape.id,
                           s"Cannot parse shape extension for vendor ${ctx.vendor}",
                           map)
          shapeExtensionDefinition.name.value()
      }
      map.key(extensionKey) match {
        case Some(entry) =>
          val dataNode =
            DataNodeParser(entry.value, parent = Some(shape.id + s"/extension/$extensionKey"))(ctx).parse()
          val extension = ShapeExtension(entry)
            .withDefinedBy(shapeExtensionDefinition)
            .withExtension(dataNode)
          shape.add(ShapeModel.CustomShapeProperties, extension)
        case None if directlyInherited.contains(shapeExtensionDefinition) =>
          if (shapeExtensionDefinition.minCount.option().exists(_ > 0)) {
            ctx.eh.violation(MissingRequiredUserDefinedFacet, shape.id, s"Missing required facet '$extensionKey'", map)
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
      ctx.closedRamlTypeShape(shape, m, syntax, typeInfo)
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
          .foreach(
            name =>
              ctx.eh.violation(UserDefinedFacetMatchesBuiltInFacets,
                               shape.id,
                               s"Custom defined facet '$name' matches built-in type facets",
                               map))
        definedCustomFacets
          .intersect(inheritedCustomFacets)
          .foreach(
            name =>
              ctx.eh.violation(UserDefinedFacetMatchesAncestorsTypeFacets,
                               shape.id,
                               s"Custom defined facet '$name' matches custom facet from inherited type",
                               map))
      })
  }

}
