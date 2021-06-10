package amf.validations

import amf.core.client.common.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.client.common.validation._
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.AmfValidation
import amf.core.internal.validation.Validations
import amf.core.internal.validation.core.ValidationSpecification
import amf.core.internal.validation.core.ValidationSpecification.PAYLOAD_VALIDATION

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
    SchemaException.id                                 -> all(VIOLATION),
  )

  override val validations: List[ValidationSpecification] = List(
    UntranslatableDraft2019Fields,
    ExampleValidationErrorSpecification,
    SchemaException,
  )
}
