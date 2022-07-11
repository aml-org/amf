package amf.shapes.internal.spec.raml.parser

import amf.shapes.internal.spec.common.parser.SpecSyntax

object Raml10ShapeSyntax extends SpecSyntax {

  private val shapeFacets = Set(
    "type",
    "default",
    "schema",
    "example",
    "examples",
    "displayName",
    "description",
    "facets",
    "xml",
    "enum"
  )

  override val nodes = Map(
    "shape"    -> shapeFacets,
    "anyShape" -> shapeFacets,
    "schemaShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "required"
    ),
    "unionShape" -> (shapeFacets + "anyOf"),
    "nodeShape" -> (shapeFacets ++ Set(
      "properties",
      "minProperties",
      "maxProperties",
      "discriminator",
      "discriminatorValue",
      "additionalProperties"
    )),
    "arrayShape" -> (shapeFacets ++ Set(
      "uniqueItems",
      "items",
      "minItems",
      "maxItems"
    )),
    "stringScalarShape" -> (shapeFacets ++ Set(
      "pattern",
      "minLength",
      "maxLength"
    )),
    "numberScalarShape" -> (shapeFacets ++ Set(
      "minimum",
      "maximum",
      "format",
      "multipleOf"
    )),
    "dateScalarShape" -> (shapeFacets + "format"),
    "fileShape" -> (shapeFacets ++ Set(
      "fileTypes",
      "minLength",
      "maxLength"
    )),
    "example" -> Set(
      "displayName",
      "description",
      "value",
      "strict"
    ),
    "xmlSerialization" -> Set(
      "attribute",
      "wrapped",
      "name",
      "namespace",
      "prefix"
    ),
    "property" -> Set(
      "required"
    ),
    "annotation" -> Set(
      "displayName",
      "description",
      "allowedTargets"
    )
  )
}
