package amf.apicontract.internal.validation.model

import amf.aml.internal.validate.AMFDialectValidations.ConstraintSeverityOverrides
import amf.apicontract.internal.validation.definitions.{ParserSideValidations, ResolutionSideValidations}
import amf.core.internal.validation.core.{ValidationProfile, ValidationSpecification}
import amf.shapes.internal.validation.model.{ImportUtils, ShapesValidationProfileBuilder, ValidationProfileBuilder}

object APIValidationProfileBuilder extends ValidationProfileBuilder with ImportUtils {

  override val staticValidations: Seq[ValidationSpecification] = ShapesValidationProfileBuilder.staticValidations ++
    ParserSideValidations.validations ++
    ResolutionSideValidations.validations

  override protected[amf] val levels: ConstraintSeverityOverrides = ShapesValidationProfileBuilder.levels ++
    ParserSideValidations.levels ++
    ResolutionSideValidations.levels

  override def profiles(): List[ValidationProfile] =
    APIRawValidations.profileToValidationMap.map { case (profile, profileValidations) =>
      buildProfileFrom(profile, profileValidations)
    }.toList
}
