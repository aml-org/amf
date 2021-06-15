package amf.shapes.internal.validation.definitions

import amf.core.client.common.validation.ProfileName
import amf.core.client.common.validation.SeverityLevels.WARNING
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.AmfResolution
import amf.core.internal.validation.Validations
import amf.core.internal.validation.core.ValidationSpecification
import amf.core.internal.validation.core.ValidationSpecification.RESOLUTION_SIDE_VALIDATION

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
