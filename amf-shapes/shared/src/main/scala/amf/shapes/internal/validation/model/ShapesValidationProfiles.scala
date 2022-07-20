package amf.shapes.internal.validation.model

import amf.core.client.common.validation.{JsonSchemaProfile, ProfileName}
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.internal.validation.model.AMFRawValidations.ShapeValidations
import amf.shapes.internal.validation.model.ShapeValidationProfiles.JsonSchemaValidationProfile
import amf.shapes.internal.validation.model.ShapesValidationProfileBuilder.buildProfileFrom

object ShapeValidationProfiles {

  val JsonSchemaValidationProfile: ValidationProfile = buildProfileFrom(JsonSchemaProfile, ShapeValidations)

  protected val shapeProfiles: Map[ProfileName, ValidationProfile] = Map(
    JsonSchemaValidationProfile.name -> JsonSchemaValidationProfile
  )

  def profile(name: ProfileName): Option[ValidationProfile] = shapeProfiles.get(name)
}

object ShapeEffectiveValidations {
  val JsonSchemaEffectiveValidations: EffectiveValidations =
    EffectiveValidations().someEffective(JsonSchemaValidationProfile)
}
