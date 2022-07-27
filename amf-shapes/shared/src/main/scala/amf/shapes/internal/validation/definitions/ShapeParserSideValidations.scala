package amf.shapes.internal.validation.definitions

import amf.core.client.common.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.client.common.validation._
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.AmfParser
import amf.core.internal.validation.Validations
import amf.core.internal.validation.core.ValidationSpecification
import amf.core.internal.validation.core.ValidationSpecification.PARSER_SIDE_VALIDATION

// noinspection TypeAnnotation
object ShapeParserSideValidations extends Validations {
  override val specification: String = PARSER_SIDE_VALIDATION
  override val namespace: Namespace  = AmfParser

  val UserDefinedFacetMatchesBuiltInFacets = validation(
    "user-defined-facets-matches-built-in",
    "User defined facet name matches built in facet of type"
  )

  val InvalidTypeExpression = validation(
    "invalid-type-expression",
    "Invalid type expression"
  )

  val InvalidDatetimeFormat = validation(
    "invalid-datetime-format",
    "Invalid format value for datetime"
  )

  val UnexpectedFileTypesSyntax = validation(
    "unexpected-file-types-syntax",
    "Unexpected 'fileTypes' syntax. Options are string or sequence"
  )

  val UnableToParseArray = validation(
    "unable-to-parse-array",
    "Unable to parse array definition"
  )

  val InvalidValueInPropertiesFacet = validation(
    "invalid-value-in-properties-facet",
    "Properties facet must be a map of key and values"
  )
  val DiscriminatorOnExtendedUnionSpecification = validation(
    "discriminator-on-extended-union",
    "Property 'discriminator' not supported in a node extending a unionShape"
  )

  val InvalidPropertyType = validation(
    "invalid-property-type",
    "Invalid property key type. Should be string"
  )

  val InvalidExternalTypeType = validation(
    "invalid-external-type-type",
    "Invalid external type type"
  )

  val SchemaDeprecated = validation(
    "schema-deprecated",
    "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead"
  )

  val InvalidAbstractDeclarationParameterInType = validation(
    "invalid-abstract-declaration-parameter-in-type",
    "Trait/Resource Type parameter in type"
  )

  val UnableToParseJsonSchema = validation(
    "unable-to-parse-json-schema",
    "Unable to parse json schema"
  )

  val InvalidJsonSchemaVersion = validation(
    "invalid-json-schema-version",
    "Invalid Json Schema version"
  )

  val MissingDiscriminatorProperty = validation(
    "missing-discriminator-property",
    "Type is missing property marked as discriminator"
  )

  val PatternPropertiesOnClosedNodeSpecification = validation(
    "pattern-properties-on-closed-node",
    "Closed node cannot define pattern properties"
  )

  val JsonSchemaInheritanceWarning = validation(
    "json-schema-inheritance",
    "Inheriting from JSON Schema"
  )

  val XmlSchemaInheritancceWarning = validation(
    "xml-schema-inheritance",
    "Inheriting from XML Schema"
  )

  val InvalidDecimalPoint = validation(
    "invalid-decimal-point",
    "Invalid decimal point"
  )

  val UserDefinedFacetMatchesAncestorsTypeFacets = validation(
    "user-defined-facets-matches-ancestor",
    "User defined facet name matches ancestor type facet"
  )

  val MissingRequiredUserDefinedFacet = validation(
    "missing-user-defined-facet",
    "Type is missing required user defined facet"
  )

  val UnableToParseShapeExtensions = validation(
    "unable-to-parse-shape-extensions",
    "Unable to parse shape extensions"
  )

  val InvalidJsonSchemaType = validation(
    "invalid-json-schema-type",
    "Invalid json schema definition type"
  )

  val InvalidAnnotationTarget = validation(
    "invalid-annotation-target",
    "Annotation not allowed in used target"
  )

  val DuplicatedPropertySpecification = validation(
    "duplicated-property",
    "Duplicated property in node"
  )

  val UnexpectedRamlScalarKey = validation(
    "unexpected-raml-scalar-key",
    "Unexpected key. Options are 'value' or annotations \\(.+\\)"
  )

  val InvalidFragmentType = validation(
    "invalid-fragment-type",
    "Invalid fragment type"
  )

  val InvalidUnevaluatedPropertiesType = validation(
    "invalid-unevaluated-properties-type",
    "unevaluatedProperties should be a boolean or a map"
  )

  val InvalidUnevaluatedItemsType = validation(
    "invalid-unevaluated-items-type",
    "unevaluatedItems should be a boolean or a map"
  )

  val ExamplesMustBeAMap = validation(
    "examples-must-be-map",
    "Examples value should be a map"
  )

  val ExamplesMustBeASeq = validation(
    "examples-must-be-seq",
    "Examples value should be an array of strings"
  )

  val ExclusivePropertiesSpecification = validation(
    "exclusive-properties-error",
    "Exclusive properties declared together"
  )

  val InvalidMediaTypeType = validation(
    "invalid-media-type-type",
    "Media type should be a string"
  )

  val InvalidTypeDefinition = validation(
    "invalid-type-definition",
    "Invalid type definition"
  )

  val InvalidRequiredArrayForSchemaVersion = validation(
    "invalid-required-array-for-schema-version",
    "Required arrays of properties not supported in JSON Schema below version draft-4"
  )

  val InvalidRequiredBooleanForSchemaVersion = validation(
    "invalid-required-boolean-for-schema-version",
    "Required property boolean value is only supported in JSON Schema draft-3"
  )

  val InvalidAdditionalPropertiesType = validation(
    "invalid-additional-properties-type",
    "additionalProperties should be a boolean or a map"
  )

  val InvalidAdditionalItemsType = validation(
    "invalid-additional-items-type",
    "additionalItems should be a boolean or a map"
  )

  val InvalidTupleType = validation(
    "invalid-tuple-type",
    "Tuple should be a sequence"
  )

  val InvalidSchemaType = validation(
    "invalid-schema-type",
    "Schema should be a string"
  )

  val InvalidXoneType = validation(
    "invalid-xone-type",
    "Xone should be a sequence"
  )

  val InvalidAndType = validation(
    "invalid-and-type",
    "And should be a sequence"
  )

  val InvalidOrType = validation(
    "invalid-or-type",
    "Or should be a sequence"
  )

  val InvalidRequiredValue = validation(
    "invalid-required-value",
    "Invalid required value"
  )

  val DuplicateRequiredItem = validation(
    "duplicate-required-item",
    "Duplicate required item"
  )

  val DiscriminatorNameRequired = validation(
    "discriminator-name-required",
    "Discriminator property name is required"
  )

  val InvalidShapeFormat = validation(
    "invalid-shape-format",
    "Invalid shape format"
  )

  val InvalidUnionType = validation(
    "invalid-union-type",
    "Union should be a sequence"
  )

  val ItemsFieldRequired = validation(
    "items-field-required",
    "'items' field is required when type is array"
  )

  val InvalidDisjointUnionType = validation(
    "invalid-disjoint-union-type",
    "Invalid type for disjoint union"
  )

  val UnexpectedVendor = validation(
    "unexpected-spec",
    "Unexpected spec"
  )

  val ReadOnlyPropertyMarkedRequired = validation(
    "read-only-property-marked-required",
    "Read only property should not be marked as required by a schema"
  )

  val ChainedReferenceSpecification = validation(
    "chained-reference-error",
    "References cannot be chained"
  )

  val UnableToSetDefaultType = validation(
    "unable-to-set-default-type",
    "Unable to set default type"
  )

  val ExclusiveSchemaType = validation(
    "exclusive-schema-type",
    "'schema' and 'type' properties are mutually exclusive"
  )

  val ExclusiveSchemasType = validation(
    "exclusive-schemas-type",
    "'schemas' and 'types' properties are mutually exclusive"
  )

  val InvalidContextNode = validation(
    "@context-must-be-object-or-string",
    "@context value must be object or string"
  )

  val InvalidCharacteristicsNode = validation(
    "@characteristics-must-be-a-seq",
    "@characteristics value must be a sequence of strings"
  )

  val InvalidPrefixReference = validation(
    "@invalid-prefix-reference",
    "the referenced prefix could not be found in the @context declarations"
  )

  val InvalidIri = validation(
    "@invalid-iri-text",
    "the text must conform the IRI format"
  )

  val InvalidBooleanSchemaForSchemaVersion = validation(
    "invalid-required-boolean-for-schema-version",
    "Boolean schemas not supported in JSON Schema below version draft-6"
  )

  val PossiblyIgnoredPatternWarning = validation(
    "possibly-ignored-pattern-warning",
    "Pattern property may be ignored if format already defines a standard pattern"
  )

  val InvalidXmlSchemaType = validation(
    "invalid-xml-schema-type",
    "Invalid xml schema type"
  )

  val UnableToParseShape = validation(
    "unable-to-parse-shape",
    "Unable to parse shape"
  )

  val JsonSchemaFragmentNotFound = validation(
    "json-schema-fragment-not-found",
    "Json schema fragment not found"
  )

  val ClosedShapeSpecification = validation(
    "closed-shape",
    "Invalid property for node"
  )

  val ClosedShapeSpecificationWarning = validation(
    "closed-shape-warning",
    "Invalid property for node"
  )

  val MissingAnnotationSchema = validation(
    "missing-annotation-schema",
    "Annotations must have a declared a schema even if there are extensions"
  )

  val AnnotationSchemaMustBeAny = validation(
    "annotation-schema-must-be-any",
    "Annotation schema must be any for api-extensions override"
  )

  val UnknownSchemaDraft = validation(
    "unknown-schema-draft",
    "Unknown JSON Schema draft version provided"
  )

  val MandatorySchema = validation(
    "mandatory-schema-key",
    "$schema entry is mandatory for JSON Schema fragments"
  )

  val JsonSchemaDefinitionNotFound = validation(
    "json-schema-definition-not-found",
    "Json schema definition not found"
  )

  val InvalidJsonSchemaReference = validation(
    "invalid-json-schema-reference",
    "Invalid JsonSchema reference"
  )

  val MultipleDefinitionKey = validation(
    "multiple-def-key",
    "Multiple definition keys found in the JSON Schema"
  )

  val IncorrectDefinitionKey = validation(
    "incorrect-def-key",
    "The definition key present in the ref is not the correct for the JSON Schema"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    InvalidShapeFormat.id            -> all(WARNING),
    JsonSchemaInheritanceWarning.id  -> all(WARNING),
    PossiblyIgnoredPatternWarning.id -> all(WARNING),
    PatternPropertiesOnClosedNodeSpecification.id -> Map(
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> WARNING
    ),
    DiscriminatorOnExtendedUnionSpecification.id -> Map(
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> WARNING
    ),
    ClosedShapeSpecificationWarning.id        -> all(WARNING),
    SchemaDeprecated.id                       -> all(WARNING),
    ReadOnlyPropertyMarkedRequired.id         -> all(WARNING),
    MissingDiscriminatorProperty.id           -> all(VIOLATION),
    InvalidRequiredBooleanForSchemaVersion.id -> all(WARNING), // TODO: should be violation
    MissingAnnotationSchema.id -> Map(
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      GrpcProfile   -> VIOLATION
    ), // TODO: Add graphqlProfile
    AnnotationSchemaMustBeAny.id -> Map(
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      GrpcProfile   -> VIOLATION
    ) // TODO: Add graphqlProfile
  )

  override val validations: List[ValidationSpecification] = List(
    UserDefinedFacetMatchesBuiltInFacets,
    UserDefinedFacetMatchesAncestorsTypeFacets,
    MissingRequiredUserDefinedFacet,
    UnableToParseShapeExtensions,
    InvalidJsonSchemaType,
    InvalidContextNode,
    InvalidCharacteristicsNode,
    InvalidPrefixReference,
    InvalidIri,
    InvalidXmlSchemaType,
    UnableToParseShape,
    JsonSchemaFragmentNotFound,
    ClosedShapeSpecification,
    ClosedShapeSpecificationWarning,
    MandatorySchema,
    UnknownSchemaDraft,
    JsonSchemaDefinitionNotFound,
    InvalidJsonSchemaReference,
    MultipleDefinitionKey,
    IncorrectDefinitionKey
  )
}
