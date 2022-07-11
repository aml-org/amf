package amf.shapes.internal.spec.raml.parser

import amf.shapes.internal.spec.common.parser.SpecSyntax

object Raml08ShapeSyntax extends SpecSyntax {

  override val nodes = Map(
    "shape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "required",
      "repeat"
    ),
    "stringScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "repeat",
      "pattern",
      "minLength",
      "maxLength",
      "required"
    ),
    "dateScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "required",
      "repeat",
      "format"
    ),
    "numberScalarShape" -> Set(
      "type",
      "default",
      "schema",
      "example",
      "examples",
      "displayName",
      "description",
      "facets",
      "enum",
      "required",
      "repeat",
      "minimum",
      "maximum",
      "format",
      "multipleOf"
    )
  )
}
