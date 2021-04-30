package amf.validations

import amf.ProfileName
import amf.core.validation.SeverityLevels.WARNING
import amf.core.validation.core.ValidationSpecification
import amf.core.validation.core.ValidationSpecification.RESOLUTION_SIDE_VALIDATION
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.AmfResolution
import amf.plugins.features.validation.Validations

object ShapeResolutionSideValidations extends Validations {
  override val specification: String = RESOLUTION_SIDE_VALIDATION
  override val namespace: Namespace  = AmfResolution

  val InvalidTypeInheritanceWarningSpecification = validation(
    "invalid-type-inheritance-warning",
    "Invalid inheritance relationship"
  )

  val InvalidTypeInheritanceErrorSpecification = validation(
    "invalid-type-inheritance",
    "Invalid inheritance relationship"
  )

  override val levels: Map[String, Map[ProfileName, String]] = Map(
    InvalidTypeInheritanceWarningSpecification.id -> all(WARNING)
  )

  override val validations: List[ValidationSpecification] = List(
    InvalidTypeInheritanceErrorSpecification,
    InvalidTypeInheritanceWarningSpecification,
  )
}
