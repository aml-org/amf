package amf.plugins.features.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace

object ParserSideValidations {

  val DuplicatedOperationStatusCodeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "duplicated-operation-status-code").iri(),
    "Status code for the  provided operation response must not be duplicated",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val MissingOperationStatusCodeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-operation-status-code").iri(),
    "Status code must be provided for an operation response",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

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

  val ExamplesMustBeAMap = ValidationSpecification(
    (Namespace.AmfParser + "examples-must-be-map").iri(),
    "Examples value should be a map",
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

  val UnknownScopeErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "unknown-scope").iri(),
    "Cannot find the scope in the security settings",
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

  val CycleReferenceError = ValidationSpecification(
    (Namespace.AmfParser + "cycle-reference").iri(),
    "Cycle in references",
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

  val InvalidTypeInheritanceWarningSpecification = ValidationSpecification(
    (Namespace.AmfParser + "invalid-type-inheritance-warning").iri(),
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

  val XmlSchemaInheratinaceWarningSpecification = ValidationSpecification(
    (Namespace.AmfParser + "xml-schema-inheritance").iri(),
    "Inheriting from XML Schema",
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

  val EmittionErrorEspecification = ValidationSpecification(
    (Namespace.AmfParser + "emittion-error").iri(),
    "Error during emittion stage",
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

  val InvalidDeclarationPathComponent = ValidationSpecification(
    (Namespace.AmfParser + "invalid-declaration-path").iri(),
    "Declaration Path component is not present in tree",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val levels: Map[String, Map[ProfileName, String]] = Map(
    DuplicatedOperationStatusCodeSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    MissingOperationStatusCodeSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.INFO
    ),
    RecursiveShapeSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.INFO
    ),
    ChainedReferenceSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    ExclusivePropertiesSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    PathTemplateUnbalancedParameters.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    OasBodyAndFormDataParameterSpecification.id -> Map(
      OasProfile   -> SeverityLevels.VIOLATION,
      Oas20Profile -> SeverityLevels.VIOLATION
    ),
    OasInvalidBodyParameter.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    OasInvalidParameterBinding.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    OasFormDataNotFileSpecification.id -> Map(
      OasProfile   -> SeverityLevels.VIOLATION,
      Oas20Profile -> SeverityLevels.VIOLATION
    ),
    MissingExtensionInReferences.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    InvalidTypeInheritanceErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    InvalidTypeInheritanceWarningSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.WARNING,
      Raml10Profile -> SeverityLevels.WARNING,
      Raml08Profile -> SeverityLevels.WARNING,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.WARNING
    ),
    JsonSchemaInheratinaceWarningSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.WARNING,
      Raml10Profile -> SeverityLevels.WARNING,
      Raml08Profile -> SeverityLevels.WARNING,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.WARNING
    ),
    XmlSchemaInheratinaceWarningSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    UnknownSecuritySchemeErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    UnknownScopeErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    ClosedShapeSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    DuplicatedPropertySpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    NamedExampleUsedInExample.id -> Map(
      RamlProfile   -> SeverityLevels.WARNING,
      Raml10Profile -> SeverityLevels.WARNING,
      Raml08Profile -> SeverityLevels.WARNING,
      OasProfile    -> SeverityLevels.INFO,
      Oas20Profile  -> SeverityLevels.INFO,
      Oas30Profile  -> SeverityLevels.INFO,
      AmfProfile    -> SeverityLevels.INFO
    ),
    DialectAmbiguousRangeSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    ParsingErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    CycleReferenceError.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    ExampleValidationErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    ParsingWarningSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.WARNING,
      Raml10Profile -> SeverityLevels.WARNING,
      Raml08Profile -> SeverityLevels.WARNING,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.WARNING
    ),
    UnsupportedExampleMediaTypeErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    UnsupportedExampleMediaTypeWarningSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.WARNING,
      Raml10Profile -> SeverityLevels.WARNING,
      Raml08Profile -> SeverityLevels.WARNING,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.WARNING
    ),
    InconsistentPropertyRangeValueSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    MissingPropertyRangeSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    MissingTermSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    MissingFragmentSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    MissingPropertySpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    ResolutionErrorSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    EmittionErrorEspecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.VIOLATION,
      Oas20Profile  -> SeverityLevels.VIOLATION,
      Oas30Profile  -> SeverityLevels.VIOLATION,
      AmfProfile    -> SeverityLevels.VIOLATION
    ),
    PatternPropertiesOnClosedNodeSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.WARNING
    ),
    DiscriminatorOnExtendedUnionSpecification.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.WARNING
    ),
    InvalidDeclarationPathComponent.id -> Map(
      RamlProfile   -> SeverityLevels.VIOLATION,
      Raml10Profile -> SeverityLevels.VIOLATION,
      Raml08Profile -> SeverityLevels.VIOLATION,
      OasProfile    -> SeverityLevels.WARNING,
      Oas20Profile  -> SeverityLevels.WARNING,
      Oas30Profile  -> SeverityLevels.WARNING,
      AmfProfile    -> SeverityLevels.WARNING
    )
  )

  def validations: List[ValidationSpecification] = List(
    DuplicatedOperationStatusCodeSpecification,
    MissingOperationStatusCodeSpecification,
    RecursiveShapeSpecification,
    NamedExampleUsedInExample,
    ChainedReferenceSpecification,
    ExclusivePropertiesSpecification,
    PathTemplateUnbalancedParameters,
    UnknownSecuritySchemeErrorSpecification,
    UnknownScopeErrorSpecification,
    JsonSchemaInheratinaceWarningSpecification,
    XmlSchemaInheratinaceWarningSpecification,
    InvalidTypeInheritanceErrorSpecification,
    InvalidTypeInheritanceWarningSpecification,
    MissingExtensionInReferences,
    ClosedShapeSpecification,
    DuplicatedPropertySpecification,
    DialectAmbiguousRangeSpecification,
    ParsingErrorSpecification,
    CycleReferenceError,
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
    EmittionErrorEspecification,
    PatternPropertiesOnClosedNodeSpecification,
    DiscriminatorOnExtendedUnionSpecification,
    InvalidDeclarationPathComponent
  )
}
