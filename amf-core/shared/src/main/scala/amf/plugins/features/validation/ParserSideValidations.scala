package amf.plugins.features.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.PARSER_SIDE_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfParser

// noinspection TypeAnnotation
object ParserSideValidations extends Validations {
  override val specification: String = PARSER_SIDE_VALIDATION
  override val namespace: Namespace = AmfParser

  val DialectError = validation(
    "dialect-error",
    "Dialect error"
  )

  val InvalidJsonSchemaType = validation(
    "invalid-json-schema-type",
    "Invalid json schema definition type"
  )

  val ExpectedVocabularyModule = validation(
    "expected-vocabulary-module",
    "Expected vocabulary module"
  )

  val InvalidShapeFormat = validation(
    "invalid-shape-format",
    "Invalid shape format"
  )

  val InvalidDialectPatch = validation(
    "invalid-dialect-patch",
    "Invalid dialect patch"
  )

  val InvalidBasePath = validation(
    "invalid-base-path",
    "Invalid base path"
  )

  val InvalidBaseUriParametersType = validation(
    "invalid-base-uri-parameters-type",
    "Invalid baseUriParameters type"
  )

  val UnusedBaseUriParameter = validation(
    "unused-base-uri-parameter",
    "Unused base uri parameter"
  )

  val ParametersWithoutBaseUri = validation(
    "parameters-without-base-uri",
    "'baseUri' not defined and 'baseUriParameters' defined."
  )

  val ParameterNameRequired = validation(
    "parameter-name-required",
    "Parameter name is required"
  )

  val InvalidServerPath = validation(
    "invalid-server-path",
    "Invalid server path"
  )

  val InvalidAbstractDeclarationParameterInType = validation(
    "invalid-abstract-declaration-parameter-in-type",
    "Trait/Resource Type parameter in type"
  )

  val InvalidSecuredByType = validation(
    "invalid-secured-by-type",
    "Invalid 'securedBy' type"
  )

  val InvalidEndpointPath = validation(
    "invalid-endpoint-path",
    "Invalid endpoint path (invalid template uri)"
  )

  val DuplicatedEndpointPath = validation(
    "duplicated-endpoint-path",
    "Duplicated endpoint path"
  )

  val DuplicatedOperationId = validation(
    "duplicated-operation-id",
    "Duplicated operation id"
  )

  val SchemaDeprecated = validation(
    "schema-deprecated",
    "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead"
  )

  val SchemasDeprecated = validation(
    "schemas-deprecated",
    "'schemas' keyword it's deprecated for 1.0 version, should use 'types' instead"
  )

  val InvalidOperationType = validation(
    "invalid-operation-type",
    "Invalid operation type"
  )

  val InvalidExternalTypeType = validation(
    "invalid-external-type-type",
    "Invalid external type type"
  )

  val InvalidXmlSchemaType = validation(
    "invalid-xml-schema-type",
    "Invalid xml schema type"
  )

  val InvalidJsonSchemaExpression = validation(
    "invalid-json-schema-expression",
    "Invalid json schema expression"
  )

  val InvalidPropertyType = validation(
    "invalid-property-type",
    "Invalid property key type. Should be string"
  )

  val UnableToParseArray = validation(
    "unable-to-parse-array",
    "Unable to parse array definition"
  )

  val InvalidDecimalPoint = validation(
    "invalid-decimal-point",
    "Invalid decimal point"
  )

  val InvalidInclude = validation(
    "invalid-include",
    "Invalid !include value"
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

  val InvalidTupleType = validation(
    "invalid-tuple-type",
    "Tuple should be a sequence"
  )

  val InvalidSchemaType = validation(
    "invalid-schema-type",
    "Schema should be a string"
  )

  val InvalidMediaTypeType = validation(
    "invalid-media-type-type",
    "Media type should be a string"
  )

  val InvalidXoneType = validation(
    "invalid-xone-type",
    "Xone should be a sequence"
  )

  val InvalidAndType = validation(
    "invalid-and-type",
    "And should be a sequence"
  )

  val InvalidUnionType = validation(
    "invalid-union-type",
    "Union should be a sequence"
  )

  val InvalidOrType = validation(
    "invalid-or-type",
    "Or should be a sequence"
  )

  val InvalidDisjointUnionType = validation(
    "invalid-disjoint-union-type",
    "Invalid type for disjoint union"
  )

  val UnexpectedVendor = validation(
    "unexpected-vendor",
    "Unexpected vendor"
  )

  val NullAbstractDeclaration = validation(
    "null-abstract-declaration",
    "Generating abstract declaration (resource type / trait)  with null value"
  )

  val InvalidAbstractDeclarationType = validation(
    "invalid-abstract-declaration-type",
    "Invalid type for declaration node"
  )

  val UnableToParseShapeExtensions = validation(
    "unable-to-parse-shape-extensions",
    "Unable to parse shape extensions"
  )

  val InvalidJsonSchemaVersion = validation(
    "invalid-json-schema-version",
    "Invalid Json Schema version"
  )

  val InvalidCrossSpec = validation(
    "invalid-cross-spec",
    "Cross spec file usage is not allowed"
  )

  val InvalidFragmentRef = validation(
    "invalid-fragment-ref",
    "References with # in RAML are not allowed"
  )

  val DeclarationNotFound = validation(
    "declaration-not-found",
    "Declaration not found"
  )

  val UriSyntaxError = validation(
    "uri-syntax-error",
    "invalid uri syntax"
  )

  val MissingIdInNode = validation(
    "missing-id-in-node",
    "Missing @id in json-ld node"
  )

  val MissingTypeInNode = validation(
    "missing-type-in-node",
    "Missing @type in json-ld node"
  )

  val CrossSecurityWarningSpecification = validation(
    "cross-security-warning",
    "Using a security scheme type from raml in oas or from oas in raml"
  )

  val InvalidModuleType = validation(
    "invalid-module-type",
    "Invalid module type"
  )

  val ExpectedModule = validation(
    "expected-module",
    "Expected Module"
  )

  val UnableToParseNode = validation(
    "parse-node-fail",
    "JsonLD @types failed to parse in node"
  )

  val UnableToParseDocument = validation(
    "parse-document-fail",
    "Unable to parse document"
  )

  val UnableToParseRdfDocument = validation(
    "parse-rdf-document-fail",
    "Unable to parse rdf document"
  )

  val NotLinkable = validation(
    "not-linkable",
    "Only linkable elements can be linked"
  )

  val DuplicatedOperationStatusCodeSpecification = validation(
    "duplicated-operation-status-code",
    "Status code for the provided operation response must not be duplicated"
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

  val ExclusivePropertiesSpecification = validation(
    "exclusive-properties-error",
    "Exclusive properties declared together"
  )

  val ExamplesMustBeAMap = validation(
    "examples-must-be-map",
    "Examples value should be a map"
  )

  val PathTemplateUnbalancedParameters = validation(
    "path-template-unbalanced-parameters",
    "Nested parameters are not allowed in path templates"
  )

  val OasBodyAndFormDataParameterSpecification = validation(
    "oas-not-body-and-form-data-parameters",
    "Operation cannot have a body parameter and a formData parameter"
  )

  val OasInvalidBodyParameter = validation(
    "oas-invalid-body-parameter",
    "Only one body parameter is allowed"
  )

  val OasInvalidParameterBinding = validation(
    "oas-invalid-parameter-binding",
    "Parameter has invalid binding"
  )

  val OasFormDataNotFileSpecification = validation(
    "oas-file-not-form-data-parameters",
    "Parameters with type file must be in formData"
  )

  val UnsupportedExampleMediaTypeErrorSpecification = validation(
    "unsupported-example-media-type",
    "Cannot validate example with unsupported media type"
  )

  val UnsupportedExampleMediaTypeWarningSpecification = validation(
    "unsupported-example-media-type-warning",
    "Cannot validate example with unsupported media type"
  )

  val UnknownSecuritySchemeErrorSpecification = validation(
    "unknown-security-scheme",
    "Cannot find the security scheme"
  )

  val MissingSecuritySchemeErrorSpecification = validation(
    "missing-security-scheme-type",
    "Missing security scheme type"
  )

  val UnknownScopeErrorSpecification = validation(
    "unknown-scope",
    "Cannot find the scope in the security settings"
  )
  val NamedExampleUsedInExample = validation(
    "named-example-used-inlined-example",
    "Named example fragments should be included in 'examples' facet"
  )
  val DialectAmbiguousRangeSpecification = validation(
    "dialect-ambiguous-range",
    "Ambiguous entity range"
  )
  val ClosedShapeSpecification = validation(
    "closed-shape",
    "Invalid property for node"
  )

  val DuplicatedPropertySpecification = validation(
    "duplicated-property",
    "Duplicated property in node"
  )

  val UnexpectedRamlScalarKey = validation(
    "unexpected-raml-scalar-key",
    "Unexpected key. Options are 'value' or annotations \\(.+\\)"
  )

  val UnexpectedFileTypesSyntax = validation(
    "unexpected-file-types-syntax",
    "Unexpected 'fileTypes' syntax. Options are string or sequence"
  )

  val MissingPropertySpecification = validation(
    "mandatory-property-shape",
    "Missing mandatory property"
  )

  val CycleReferenceError = validation(
    "cycle-reference",
    "Cycle in references"
  )

  val JsonSchemaInheratinaceWarningSpecification = validation(
    "json-schema-inheritance",
    "Inheriting from JSON Schema"
  )

  val XmlSchemaInheratinaceWarningSpecification = validation(
    "xml-schema-inheritance",
    "Inheriting from XML Schema"
  )

  val InconsistentPropertyRangeValueSpecification = validation(
    "inconsistent-property-range-value",
    "Range value does not match the expected type"
  )

  val MissingPropertyRangeSpecification = validation(
    "missing-node-mapping-range-term",
    "Missing property range term"
  )

  val MissingTermSpecification = validation(
    "missing-vocabulary-term",
    "Missing vocabulary term"
  )

  val MissingVocabulary = validation(
    "missing-vocabulary",
    "Missing vocabulary"
  )

  val MissingFragmentSpecification = validation(
    "missing-dialect-fragment",
    "Missing dialect fragment"
  )

  val InvalidEndpointType = validation(
    "invalid-endpoint-type",
    "Invalid endpoint type"
  )

  val InvalidParameterType = validation(
    "invalid-parameter-type",
    "Invalid parameter type"
  )

  val UnableToParseShape = validation(
    "unable-to-parse-shape",
    "Unable to parse shape"
  )

  val JsonSchemaFragmentNotFound = validation(
    "json-schema-fragment-not-found",
    "Json schema fragment not found"
  )

  val PatternPropertiesOnClosedNodeSpecification = validation(
    "pattern-properties-on-closed-node",
    "Closed node cannot define pattern properties"
  )

  val DiscriminatorOnExtendedUnionSpecification = validation(
    "discriminator-on-extended-union",
    "Property discriminator not supported in a node extending a unionShape"
  )

  val UnresolvedReference = validation(
    "unresolved-reference",
    "Unresolved reference"
  )

  val UnresolvedParameter = validation(
    "unresolved-parameter",
    "Unresolved parameter"
  )

  val UnableToParseJsonSchema = validation(
    "unable-to-parse-json-schema",
    "Unable to parse json schema"
  )

  val InvalidAnnotationType = validation(
    "invalid-annotation-type",
    "Invalid annotation type"
  )

  val InvalidFragmentType = validation(
    "invalid-fragment-type",
    "Invalid fragment type"
  )

  val InvalidTypesType = validation(
    "invalid-types-type",
    "Invalid types type"
  )

  val InvalidDocumentationType = validation(
    "invalid-documentation-type",
    "Invalid documentation type"
  )

  val InvalidAllowedTargetsType = validation(
    "invalid-allowed-targets-type",
    "Invalid allowedTargets type"
  )

  val InvalidExtensionsType = validation(
    "invalid-extension-type",
    "Invalid extension type"
  )

  val ModuleNotFound = validation(
    "module-not-found",
    "Module not found"
  )

  val ExternalFragmentWarning = validation(
    "external-fragment-warning",
    "External fragment will be created"
  )

  val InvalidTypeExpression = validation(
    "invalid-type-expression",
    "Invalid type expression"
  )

  val UnexpectedReference = validation(
    "unexpected-reference",
    "Unexpected reference"
  )

  val SyamlError = validation(
    "syaml-error",
    "Syaml error"
  )

  val SyamlWarning = validation(
    "syaml-warning",
    "Syaml warning"
  )

  val NodeNotFound = validation(
    "node-not-found",
    "Builder for model not found"
  )

  val ExampleValidationErrorSpecification = validation(
    "example-validation-error",
    "Example does not validate type"
  )

  val ReadOnlyPropertyMarkedRequired = validation(
    "read-only-property-marked-required",
    "Read only property should not be marked as required by a schema"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    OasBodyAndFormDataParameterSpecification.id -> Map(
      OasProfile   -> VIOLATION,
      Oas20Profile -> VIOLATION
    ),
    OasInvalidBodyParameter.id    -> all(VIOLATION),
    OasInvalidParameterBinding.id -> all(VIOLATION),
    OasFormDataNotFileSpecification.id -> Map(
      OasProfile   -> VIOLATION,
      Oas20Profile -> VIOLATION
    ),
    JsonSchemaInheratinaceWarningSpecification.id -> all(WARNING),
    NamedExampleUsedInExample.id -> Map(
      RamlProfile   -> VIOLATION,
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      OasProfile    -> SeverityLevels.INFO,
      Oas20Profile  -> SeverityLevels.INFO,
      Oas30Profile  -> SeverityLevels.INFO,
      AmfProfile    -> SeverityLevels.INFO
    ),
    SyamlWarning.id                                    -> all(WARNING),
    UnsupportedExampleMediaTypeWarningSpecification.id -> all(WARNING),
    PatternPropertiesOnClosedNodeSpecification.id -> Map(
      RamlProfile   -> VIOLATION,
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      OasProfile    -> WARNING,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> WARNING
    ),
    DiscriminatorOnExtendedUnionSpecification.id -> Map(
      RamlProfile   -> VIOLATION,
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      OasProfile    -> WARNING,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> WARNING
    ),
    NullAbstractDeclaration.id -> all(WARNING),
    SchemaDeprecated.id        -> all(WARNING),
    SchemasDeprecated.id       -> all(WARNING),
    ExternalFragmentWarning.id -> all(WARNING),
    UnusedBaseUriParameter.id  -> all(WARNING),
    InvalidShapeFormat.id      -> all(WARNING),
    CrossSecurityWarningSpecification.id      -> all(WARNING),
    ExampleValidationErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    ReadOnlyPropertyMarkedRequired.id -> all(WARNING),
  )

  override val validations: List[ValidationSpecification] = List(
    DuplicatedOperationStatusCodeSpecification,
    NamedExampleUsedInExample,
    ChainedReferenceSpecification,
    ExclusivePropertiesSpecification,
    PathTemplateUnbalancedParameters,
    UnknownSecuritySchemeErrorSpecification,
    MissingSecuritySchemeErrorSpecification,
    UnknownScopeErrorSpecification,
    JsonSchemaInheratinaceWarningSpecification,
    XmlSchemaInheratinaceWarningSpecification,
    ClosedShapeSpecification,
    DuplicatedPropertySpecification,
    DialectAmbiguousRangeSpecification,
    CycleReferenceError,
    ExamplesMustBeAMap,
    UnsupportedExampleMediaTypeErrorSpecification,
    UnsupportedExampleMediaTypeWarningSpecification,
    InconsistentPropertyRangeValueSpecification,
    MissingPropertyRangeSpecification,
    MissingTermSpecification,
    MissingFragmentSpecification,
    MissingPropertySpecification,
    OasInvalidBodyParameter,
    PatternPropertiesOnClosedNodeSpecification,
    DiscriminatorOnExtendedUnionSpecification,
    OasFormDataNotFileSpecification,
    OasBodyAndFormDataParameterSpecification,
    OasInvalidParameterBinding,
    NotLinkable,
    UnresolvedReference,
    SyamlError,
    SyamlWarning,
    NodeNotFound,
    UnableToParseDocument,
    UnableToParseNode,
    ExpectedModule,
    InvalidModuleType,
    MissingIdInNode,
    MissingTypeInNode,
    UriSyntaxError,
    UnableToParseRdfDocument,
    DeclarationNotFound,
    MissingVocabulary,
    InvalidInclude,
    UnableToParseJsonSchema,
    UnexpectedRamlScalarKey,
    UnableToParseShapeExtensions,
    InvalidAbstractDeclarationType,
    NullAbstractDeclaration,
    UnexpectedVendor,
    InvalidDisjointUnionType,
    InvalidUnionType,
    InvalidOrType,
    InvalidAndType,
    InvalidXoneType,
    InvalidAdditionalPropertiesType,
    InvalidRequiredArrayForSchemaVersion,
    InvalidRequiredBooleanForSchemaVersion,
    InvalidSchemaType,
    UnableToSetDefaultType,
    InvalidTypeDefinition,
    InvalidTupleType,
    UnableToParseArray,
    InvalidDecimalPoint,
    InvalidPropertyType,
    JsonSchemaFragmentNotFound,
    InvalidJsonSchemaExpression,
    InvalidXmlSchemaType,
    InvalidExternalTypeType,
    InvalidAbstractDeclarationParameterInType,
    ExclusiveSchemaType,
    SchemaDeprecated,
    UnresolvedParameter,
    ParameterNameRequired,
    InvalidSecuredByType,
    InvalidEndpointPath,
    DuplicatedEndpointPath,
    DuplicatedOperationId,
    InvalidOperationType,
    InvalidServerPath,
    ParametersWithoutBaseUri,
    UnusedBaseUriParameter,
    InvalidBaseUriParametersType,
    InvalidBasePath,
    InvalidParameterType,
    InvalidMediaTypeType,
    InvalidJsonSchemaVersion,
    InvalidCrossSpec,
    InvalidEndpointType,
    UnableToParseShape,
    InvalidAnnotationType,
    InvalidFragmentType,
    InvalidTypesType,
    SchemasDeprecated,
    ExclusiveSchemasType,
    InvalidDocumentationType,
    InvalidAllowedTargetsType,
    ExternalFragmentWarning,
    InvalidTypeExpression,
    InvalidExtensionsType,
    ModuleNotFound,
    UnexpectedReference,
    InvalidShapeFormat,
    UnexpectedFileTypesSyntax,
    InvalidDialectPatch,
    DialectError,
    ExampleValidationErrorSpecification,
    InvalidJsonSchemaType,
    InvalidFragmentRef,
    CrossSecurityWarningSpecification,
    ReadOnlyPropertyMarkedRequired
  )
}
