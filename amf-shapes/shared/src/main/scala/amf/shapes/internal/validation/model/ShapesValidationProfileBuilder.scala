package amf.shapes.internal.validation.model

import amf.aml.internal.validate.AMFDialectValidations
import amf.aml.internal.validate.AMFDialectValidations.ConstraintSeverityOverrides
import amf.core.internal.validation.core.{ValidationProfile, ValidationSpecification}
import amf.core.internal.validation.{CoreParserValidations, CorePayloadValidations, RenderSideValidations}
import amf.shapes.internal.validation.definitions.{ShapeParserSideValidations, ShapePayloadValidations}

object ShapesValidationProfileBuilder extends ValidationProfileBuilder with ImportUtils {

  override val staticValidations: Seq[ValidationSpecification] = AMFDialectValidations.staticValidations ++
    RenderSideValidations.validations ++
    ShapePayloadValidations.validations ++
    ShapeParserSideValidations.validations ++
    CorePayloadValidations.validations ++
    CoreParserValidations.validations

  override protected[amf] val levels: ConstraintSeverityOverrides = AMFDialectValidations.levels ++
    RenderSideValidations.levels ++
    ShapePayloadValidations.levels ++
    ShapeParserSideValidations.levels ++
    CorePayloadValidations.levels ++
    CoreParserValidations.levels

  override def profiles(): List[ValidationProfile] =
    AMFRawValidations.profileToValidationMap.map { case (profile, profileValidations) =>
      buildProfileFrom(profile, profileValidations)
    }.toList
}
