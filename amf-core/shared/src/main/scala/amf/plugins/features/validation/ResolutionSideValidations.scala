package amf.plugins.features.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.RESOLUTION_SIDE_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfResolution

// noinspection TypeAnnotation
object ResolutionSideValidations extends Validations {
  override val specification: String = RESOLUTION_SIDE_VALIDATION
  override val namespace: Namespace  = AmfResolution

  val ResolutionValidation = validation(
    "resolution-validation",
    "Default resolution validation"
  )

  val UnsupportedPipeline = validation(
    "unsupported-pipeline",
    "Unsupported pipeline"
  )

  val MissingExtensionInReferences = validation(
    "missing-extension",
    "Missing extension in reference"
  )

  val RecursiveShapeSpecification = validation(
    "recursive-shape",
    "Recursive shape",
    Some("Recursive type"),
    Some("Recursive schema")
  )

  val InvalidTypeInheritanceWarningSpecification = validation(
    "invalid-type-inheritance-warning",
    "Invalid inheritance relationship"
  )

  val InvalidTypeInheritanceErrorSpecification = validation(
    "invalid-type-inheritance",
    "Invalid inheritance relationship"
  )

  val NestedEndpoint = validation(
    "nested-endpoint",
    "Nested endpoints"
  )

  val ParseResourceTypeFail = validation(
    "parse-resource-type-fail",
    "Failed while parsing an endpoint from a resource type"
  )

  val UnequalMediaTypeDefinitionsInExtendsPayloads = validation(
    "unequal-media-type-definitions-in-extends-payloads",
    "Payload media types in traits/resource types and in operations/endpoints should be defined " +
      "equivalently (inline or using global default)"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    RecursiveShapeSpecification.id -> Map(
      RamlProfile   -> VIOLATION,
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      OasProfile    -> WARNING,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> SeverityLevels.INFO
    ),
    InvalidTypeInheritanceWarningSpecification.id -> all(WARNING)
  )

  override val validations: List[ValidationSpecification] = List(
    ResolutionValidation,
    MissingExtensionInReferences,
    RecursiveShapeSpecification,
    InvalidTypeInheritanceErrorSpecification,
    InvalidTypeInheritanceWarningSpecification,
    NestedEndpoint,
    ParseResourceTypeFail,
    UnsupportedPipeline,
    UnequalMediaTypeDefinitionsInExtendsPayloads
  )
}
