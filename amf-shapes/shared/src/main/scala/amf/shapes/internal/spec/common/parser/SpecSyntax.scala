package amf.shapes.internal.spec.common.parser

/** Created by pedro.colunga on 11/9/17.
  */
trait SpecSyntax {
  val nodes: Map[String, Set[String]]
  val JsonSchemaFields: Set[String] = Set(
    "$ref",
    "$schema",
    "format",
    "title",
    "description",
    "maximum",
    "exclusiveMaximum",
    "minimum",
    "exclusiveMinimum",
    "maxLength",
    "minLength",
    "nullable",
    "pattern",
    "maxItems",
    "minItems",
    "uniqueItems",
    "maxProperties",
    "minProperties",
    "required",
    "enum",
    "type",
    "items",
    "additionalItems",
    "collectionFormat",
    "allOf",
    "properties",
    "additionalProperties",
    "discriminator",
    "readOnly",
    "writeOnly",
    "xml",
    "deprecated",
    "externalDocs",
    "allOf",
    "anyOf",
    "oneOf",
    "not",
    "dependencies",
    "multipleOf",
    "default",
    "example",
    "id",
    "name",
    "patternProperties"
  )
}

object SpecSyntax {
  val empty: SpecSyntax = new SpecSyntax {
    override val nodes: Map[String, Set[String]] = Map.empty
  }
}
