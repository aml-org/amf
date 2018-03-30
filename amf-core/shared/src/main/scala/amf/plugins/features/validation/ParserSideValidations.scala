package amf.plugins.features.validation

import amf.ProfileNames
import amf.core.validation.SeverityLevels
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace

object ParserSideValidations {

  val OasBodyAndFormDataParameterSpecification = ValidationSpecification(
    (Namespace.AmfParser + "oas-not-body-and-form-data-parameters").iri(),
    "Operation cannot have a body parameter and a formData parameter",
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

  val UnknownSecuritySchemeErrorSpecification = ValidationSpecification(
    (Namespace.AmfParser + "unknown-security-scheme").iri(),
    "Cannot find the security scheme",
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


  val levels: Map[String, Map[String, String]] = Map(
    OasBodyAndFormDataParameterSpecification.id() -> Map(
      ProfileNames.OAS  -> SeverityLevels.VIOLATION
    ),

    OasFormDataNotFileSpecification.id() -> Map(
      ProfileNames.OAS  -> SeverityLevels.VIOLATION
    ),

    InvalidTypeInheritanceErrorSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    JsonSchemaInheratinaceWarningSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.WARNING,
      ProfileNames.OAS  -> SeverityLevels.WARNING,
      ProfileNames.AMF  -> SeverityLevels.WARNING
    ),
    UnknownSecuritySchemeErrorSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    ClosedShapeSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    DuplicatedPropertySpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    DialectAmbiguousRangeSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    ParsingErrorSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    ExampleValidationErrorSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.WARNING,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    ParsingWarningSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.WARNING,
      ProfileNames.OAS  -> SeverityLevels.WARNING,
      ProfileNames.AMF  -> SeverityLevels.WARNING
    ),
    UnsupportedExampleMediaTypeErrorSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.WARNING,
      ProfileNames.OAS  -> SeverityLevels.WARNING,
      ProfileNames.AMF  -> SeverityLevels.WARNING
    ),
    InconsistentPropertyRangeValueSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    MissingPropertyRangeSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    MissingTermSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    MissingFragmentSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    MissingPropertySpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    )
  )

  def validations: List[ValidationSpecification] = List(
    UnknownSecuritySchemeErrorSpecification,
    JsonSchemaInheratinaceWarningSpecification,
    InvalidTypeInheritanceErrorSpecification,
    ClosedShapeSpecification,
    DuplicatedPropertySpecification,
    DialectAmbiguousRangeSpecification,
    ParsingErrorSpecification,
    ParsingWarningSpecification,
    ExampleValidationErrorSpecification,
    UnsupportedExampleMediaTypeErrorSpecification,
    InconsistentPropertyRangeValueSpecification,
    MissingPropertyRangeSpecification,
    MissingTermSpecification,
    MissingFragmentSpecification,
    MissingPropertySpecification
  )
}
