package amf.validations

import amf.core.validation.SeverityLevels._
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.RESOLUTION_SIDE_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfResolution
import amf.plugins.features.validation.Validations
import amf._

// noinspection TypeAnnotation
object ResolutionSideValidations extends Validations {
  override val specification: String = RESOLUTION_SIDE_VALIDATION
  override val namespace: Namespace  = AmfResolution

  val UnsupportedPipeline = validation(
    "unsupported-pipeline",
    "Unsupported pipeline"
  )

  val MissingExtensionInReferences = validation(
    "missing-extension",
    "Missing extension in reference"
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

  val UnequalMediaTypeDefinitionsInExtendsPayloads = validation(
    "unequal-media-type-definitions-in-extends-payloads",
    "Cannot merge payloads with explicit and implicit media types"
  )

  val ParseResourceTypeFail = validation(
    "parse-resource-type-fail",
    "Failed while parsing an endpoint from a resource type"
  )

  val InvalidConsumesWithFileParameter = validation(
    "invalid-consumes-with-file-parameter",
    "File parameters must have specific consumes property defined"
  )

  val ExamplesWithInvalidMimeType = validation(
    "examples-with-invalid-mime-type",
    "Mime type defined in 'examples' must be present in a 'produces' property"
  )

  val ExamplesWithNoSchemaDefined = validation(
    "examples-with-no-schema-defined",
    "When schema is undefined, 'examples' facet is invalid as no content is returned as part of the response"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    InvalidTypeInheritanceWarningSpecification.id -> all(WARNING)
  )

  override val validations: List[ValidationSpecification] = List(
    MissingExtensionInReferences,
    NestedEndpoint,
    UnequalMediaTypeDefinitionsInExtendsPayloads,
    ParseResourceTypeFail,
    UnsupportedPipeline,
    InvalidConsumesWithFileParameter,
    ExamplesWithInvalidMimeType,
    ExamplesWithNoSchemaDefined
  )
}
