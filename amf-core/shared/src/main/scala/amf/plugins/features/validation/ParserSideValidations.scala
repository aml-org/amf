package amf.plugins.features.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace

object ParserSideValidations {

  val ChainedReferenceSpecification = ValidationSpecification(
    (Namespace.AmfParser + "chained-reference-error").iri(),
    "References cannot be chained",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val RecursiveShapeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "recursive-shape").iri(),
    "Recursive shape",
    Some("Recursive type"),
    Some("Recursive schema"),
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val ExclusivePropertiesSpecification = ValidationSpecification(
    (Namespace.AmfParser + "exclusive-properties-error").iri(),
    "Exclusive properties declared together",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val PathTemplateUnbalancedParameters = ValidationSpecification(
    (Namespace.AmfParser + "path-template-unbalanced-parameters").iri(),
    "Nested parameters are not allowed in path templates",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val OasBodyAndFormDataParameterSpecification = ValidationSpecification(
    (Namespace.AmfParser + "oas-not-body-and-form-data-parameters").iri(),
    "Operation cannot have a body parameter and a formData parameter",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val OasInvalidBodyParameter = ValidationSpecification(
    (Namespace.AmfParser + "oas-invalid-body-parameter").iri(),
    "Only one body parameter is allowed",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val OasInvalidParameterBinding = ValidationSpecification(
    (Namespace.AmfParser + "oas-invalid-parameter-binding").iri(),
    "Parameter has invalid binding",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val OasFormDataNotFileSpecification = ValidationSpecification(
    (Namespace.AmfParser + "oas-file-not-form-data-parameters").iri(),
    "Parameters with type file must be in formData",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val ExampleValidationErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "example-validation-error").iri(),
    "Example does not validate type",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val UnsupportedExampleMediaTypeErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "unsupported-example-media-type").iri(),
    "Cannot validate example with unsupported media type",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val UnsupportedExampleMediaTypeWarningSpecification = ValidationSpecification(
    (Namespace.AmfParser + "unsupported-example-media-type-warning").iri(),
    "Cannot validate example with unsupported media type",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val UnknownSecuritySchemeErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "unknown-security-scheme").iri(),
    "Cannot find the security scheme",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )
  val NamedExampleUsedInExample = ValidationSpecification(
    (Namespace.AmfParser + "named-example-used-inlined-example").iri(),
    "Named example should not be used as inline examples",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )
  val DialectAmbiguousRangeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "dialect-ambiguous-range").iri(),
    "Ambiguous entity range",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )
  val ClosedShapeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "closed-shape").iri(),
    "Invalid property for node",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val DuplicatedPropertySpecification = ValidationSpecification(
    (Namespace.AmfParser + "duplicated-property").iri(),
    "Duplicated property in node",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val MissingPropertySpecification = ValidationSpecification(
    (Namespace.AmfParser + "mandatory-property-shape").iri(),
    "Missing mandatory property",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val ParsingErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "parsing-error").iri(),
    "Parsing error",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val InvalidTypeInheritanceErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "invalid-type-inheritance").iri(),
    "Invalid inheritance relationship",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val MissingExtensionInReferences = ValidationSpecification(
    (Namespace.AmfParser + "missing-extension").iri(),
    "Missing extension in reference",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val JsonSchemaInheratinaceWarningSpecification = ValidationSpecification(
    (Namespace.AmfParser + "json-schema-inheritance").iri(),
    "Inheriting from JSON Schema",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val ParsingWarningSpecification = ValidationSpecification(
    (Namespace.AmfParser + "parsing-warning").iri(),
    "Parsing warning",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val InconsistentPropertyRangeValueSpecification = ValidationSpecification(
    (Namespace.AmfParser + "inconsistent-property-range-value").iri(),
    "Range value does not match the expected type",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val MissingPropertyRangeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-node-mapping-range-term").iri(),
    "Missing property range term",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val MissingTermSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-vocabulary-term").iri(),
    "Missing vocabulary term",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val MissingFragmentSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-dialect-fragment").iri(),
    "Missing dialect fragment",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val ResolutionErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "resolution-error").iri(),
    "Error during resolution stage",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val PatternPropertiesOnClosedNodeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "pattern-properties-on-closed-node").iri(),
    "Closed node cannot define pattern properties",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val DiscriminatorOnExtendedUnionSpecification = ValidationSpecification(
    (Namespace.AmfParser + "discriminator-on-extended-union").iri(),
    "Property discriminator not supported in a node extending a unionShape",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val levels: Map[String, Map[ProfileName, String]] = Map(
    RecursiveShapeSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.WARNING,
      OAS3Profile   -> SeverityLevels.WARNING,
      AMFProfile    -> SeverityLevels.INFO
    ),
    ChainedReferenceSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    ExclusivePropertiesSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    PathTemplateUnbalancedParameters.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    OasBodyAndFormDataParameterSpecification.id -> Map(
      OASProfile -> SeverityLevels.VIOLATION,
      OASProfile -> SeverityLevels.VIOLATION
    ),
    OasInvalidBodyParameter.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    OasInvalidParameterBinding.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    OasFormDataNotFileSpecification.id -> Map(
      OASProfile -> SeverityLevels.VIOLATION,
      OASProfile -> SeverityLevels.VIOLATION
    ),
    MissingExtensionInReferences.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    InvalidTypeInheritanceErrorSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    JsonSchemaInheratinaceWarningSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.WARNING,
      RAML08Profile -> SeverityLevels.WARNING,
      OASProfile    -> SeverityLevels.WARNING,
      OAS3Profile   -> SeverityLevels.WARNING,
      AMFProfile    -> SeverityLevels.WARNING
    ),
    UnknownSecuritySchemeErrorSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    ClosedShapeSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    DuplicatedPropertySpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    NamedExampleUsedInExample.id -> Map(
      RAMLProfile   -> SeverityLevels.WARNING,
      RAML08Profile -> SeverityLevels.WARNING,
      OASProfile    -> SeverityLevels.INFO,
      OAS3Profile   -> SeverityLevels.INFO,
      AMFProfile    -> SeverityLevels.INFO
    ),
    DialectAmbiguousRangeSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    ParsingErrorSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    ExampleValidationErrorSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.WARNING,
      OAS3Profile   -> SeverityLevels.WARNING,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    ParsingWarningSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.WARNING,
      RAML08Profile -> SeverityLevels.WARNING,
      OASProfile    -> SeverityLevels.WARNING,
      OAS3Profile   -> SeverityLevels.WARNING,
      AMFProfile    -> SeverityLevels.WARNING
    ),
    UnsupportedExampleMediaTypeErrorSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    UnsupportedExampleMediaTypeWarningSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.WARNING,
      RAML08Profile -> SeverityLevels.WARNING,
      OASProfile    -> SeverityLevels.WARNING,
      OAS3Profile   -> SeverityLevels.WARNING,
      AMFProfile    -> SeverityLevels.WARNING
    ),
    InconsistentPropertyRangeValueSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    MissingPropertyRangeSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    MissingTermSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    MissingFragmentSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    MissingPropertySpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    ResolutionErrorSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.VIOLATION,
      OAS3Profile   -> SeverityLevels.VIOLATION,
      AMFProfile    -> SeverityLevels.VIOLATION
    ),
    PatternPropertiesOnClosedNodeSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.WARNING,
      OAS3Profile   -> SeverityLevels.WARNING,
      AMFProfile    -> SeverityLevels.WARNING
    ),
    DiscriminatorOnExtendedUnionSpecification.id -> Map(
      RAMLProfile   -> SeverityLevels.VIOLATION,
      RAML08Profile -> SeverityLevels.VIOLATION,
      OASProfile    -> SeverityLevels.WARNING,
      OAS3Profile   -> SeverityLevels.WARNING,
      AMFProfile    -> SeverityLevels.WARNING
    )
  )

  def validations: List[ValidationSpecification] = List(
    RecursiveShapeSpecification,
    NamedExampleUsedInExample,
    ChainedReferenceSpecification,
    ExclusivePropertiesSpecification,
    PathTemplateUnbalancedParameters,
    UnknownSecuritySchemeErrorSpecification,
    JsonSchemaInheratinaceWarningSpecification,
    InvalidTypeInheritanceErrorSpecification,
    MissingExtensionInReferences,
    ClosedShapeSpecification,
    DuplicatedPropertySpecification,
    DialectAmbiguousRangeSpecification,
    ParsingErrorSpecification,
    ParsingWarningSpecification,
    ExampleValidationErrorSpecification,
    UnsupportedExampleMediaTypeErrorSpecification,
    UnsupportedExampleMediaTypeWarningSpecification,
    InconsistentPropertyRangeValueSpecification,
    MissingPropertyRangeSpecification,
    MissingTermSpecification,
    MissingFragmentSpecification,
    MissingPropertySpecification,
    OasInvalidBodyParameter,
    ResolutionErrorSpecification,
    PatternPropertiesOnClosedNodeSpecification,
    DiscriminatorOnExtendedUnionSpecification
  )
}
