package amf.apicontract.internal.validation.model

import amf.apicontract.internal.validation.model.AMFRawValidations._
import amf.apicontract.internal.validation.model.ApiValidationProfiles.{
  AmfValidationProfile,
  Async20ValidationProfile,
  Oas20ValidationProfile,
  Oas30ValidationProfile,
  Raml08ValidationProfile,
  Raml10ValidationProfile
}
import amf.apicontract.internal.validation.model.DefaultAMFValidations.buildProfileFrom
import amf.core.client.common.validation._
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationProfile

object ApiValidationProfiles {

  val Raml08ValidationProfile: ValidationProfile = buildProfileFrom(Raml08Profile, Raml08Validations)
  val Raml10ValidationProfile: ValidationProfile = buildProfileFrom(Raml10Profile, Raml10Validations)

  val Oas20ValidationProfile: ValidationProfile = buildProfileFrom(Oas20Profile, Oas20Validations)
  val Oas30ValidationProfile: ValidationProfile = buildProfileFrom(Oas30Profile, Oas30Validations)

  val Async20ValidationProfile: ValidationProfile = buildProfileFrom(Async20Profile, Async20Validations)
  val AmfValidationProfile: ValidationProfile     = buildProfileFrom(AmfProfile, AmfValidations)

  protected val apiProfiles: Map[ProfileName, ValidationProfile] = Map(
    Raml08ValidationProfile.name  -> Raml08ValidationProfile,
    Raml10ValidationProfile.name  -> Raml10ValidationProfile,
    Oas20ValidationProfile.name   -> Oas20ValidationProfile,
    Oas30ValidationProfile.name   -> Oas30ValidationProfile,
    Async20ValidationProfile.name -> Async20ValidationProfile,
    AmfValidationProfile.name     -> AmfValidationProfile
  )

  def profile(name: ProfileName): Option[ValidationProfile] = apiProfiles.get(name)
}

object ApiEffectiveValidations {
  val Raml08EffectiveValidations: EffectiveValidations = EffectiveValidations().someEffective(Raml08ValidationProfile)
  val Raml10EffectiveValidations: EffectiveValidations = EffectiveValidations().someEffective(Raml10ValidationProfile)

  val Oas20EffectiveValidations: EffectiveValidations = EffectiveValidations().someEffective(Oas20ValidationProfile)
  val Oas30EffectiveValidations: EffectiveValidations = EffectiveValidations().someEffective(Oas30ValidationProfile)

  val Async20EffectiveValidations: EffectiveValidations =
    EffectiveValidations().someEffective(Async20ValidationProfile)
  val AmfEffectiveValidations: EffectiveValidations = EffectiveValidations().someEffective(AmfValidationProfile)
}
