package amf.validations

import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.PAYLOAD_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfValidation
import amf.plugins.features.validation.Validations
import amf._

// noinspection TypeAnnotation
object ShapePayloadValidations extends Validations {
  override val specification: String = PAYLOAD_VALIDATION
  override val namespace: Namespace  = AmfValidation

  val UntranslatableDraft2019Fields = validation(
    "untranslatable-draft-2019-fields",
    "The schema to validate against has fields that will not be used for validation"
  )

  val ExampleValidationErrorSpecification = validation(
    "example-validation-error",
    "Example does not validate type"
  )

  val SchemaException = validation(
    "schema-exception",
    "Schema exception"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    UntranslatableDraft2019Fields.id -> all(WARNING),
    ExampleValidationErrorSpecification.id -> Map(
      Raml10Profile -> VIOLATION,
      Raml08Profile -> VIOLATION,
      Oas20Profile  -> WARNING,
      Oas30Profile  -> WARNING,
      AmfProfile    -> VIOLATION
    ),
    SchemaException.id -> all(VIOLATION)
  )

  override val validations: List[ValidationSpecification] = List(
    UntranslatableDraft2019Fields,
    ExampleValidationErrorSpecification,
    SchemaException
  )
}
