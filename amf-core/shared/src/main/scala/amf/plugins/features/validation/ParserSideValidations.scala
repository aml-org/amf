package amf.plugins.features.validation

import amf.ProfileNames
import amf.core.validation.SeverityLevels
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace

object ParserSideValidations {

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

  val DialectAmbiguousRangeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "dialect-ambiguous-range").iri(),
    "Ambiguous entity range",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val DialectExpectingMap = ValidationSpecification(
    (Namespace.AmfParser + "dialect-expecting-map").iri(),
    "Expecting map node",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )
  val DialectExtendIssue = ValidationSpecification(
    (Namespace.AmfParser + "dialect-extend-issue").iri(),
    "Extend related issue",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val DialectUnresolvableReference = ValidationSpecification(
    (Namespace.AmfParser + "dialect-unresolvable-reference").iri(),
    "Unresolvable Reference",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )
  val DialectNodeRangeShouldBeDialect = ValidationSpecification(
    (Namespace.AmfParser + "dialect-node-range-should-be-dialect").iri(),
    "Dialect Node Range should be dialect",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val ClosedShapeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "closed-shape").iri(),
    "invalid property for node",
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

  val ParsingWarningSpecification = ValidationSpecification(
    (Namespace.AmfParser + "parsing-warning").iri(),
    "Parsing warning",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  val levels: Map[String, Map[String, String]] = Map(
    ClosedShapeSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    DialectAmbiguousRangeSpecification.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    DialectExpectingMap.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    DialectUnresolvableReference.id() -> Map(
      ProfileNames.RAML -> SeverityLevels.VIOLATION,
      ProfileNames.OAS  -> SeverityLevels.VIOLATION,
      ProfileNames.AMF  -> SeverityLevels.VIOLATION
    ),
    DialectNodeRangeShouldBeDialect.id() -> Map(
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
    )
  )

  def validations: List[ValidationSpecification] = List(
    ClosedShapeSpecification,
    DialectAmbiguousRangeSpecification,
    ParsingErrorSpecification,
    ParsingWarningSpecification,
    ExampleValidationErrorSpecification,
    UnsupportedExampleMediaTypeErrorSpecification,
    DialectExpectingMap,
    DialectUnresolvableReference,
    DialectNodeRangeShouldBeDialect
  )
}
