package amf.validations

import amf.core.validation.SeverityLevels.{VIOLATION, WARNING}
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.PAYLOAD_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfValidation
import amf.plugins.features.validation.Validations
import amf._

// noinspection TypeAnnotation
object PayloadValidations extends Validations {
  override val specification: String = PAYLOAD_VALIDATION
  override val namespace: Namespace  = AmfValidation

  val UnsupportedExampleMediaTypeWarningSpecification = validation(
    "unsupported-example-media-type-warning",
    "Cannot validate example with unsupported media type"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    UnsupportedExampleMediaTypeWarningSpecification.id -> all(WARNING)
  )

  override val validations: List[ValidationSpecification] = List(
    UnsupportedExampleMediaTypeWarningSpecification,
  )
}
