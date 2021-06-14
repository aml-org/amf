package amf.validations

import amf.core.client.common.validation.ProfileName
import amf.core.client.common.validation.SeverityLevels.WARNING
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.AmfValidation
import amf.core.internal.validation.Validations
import amf.core.internal.validation.core.ValidationSpecification
import amf.core.internal.validation.core.ValidationSpecification.PAYLOAD_VALIDATION

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
