package amf.plugins.document.webapi.validation

import amf.core.validation.core.ValidationProfile
import amf.plugins.document.webapi.validation.AMFRawValidations._
import amf.plugins.document.webapi.validation.DefaultAMFValidations.buildProfileFrom
import amf._

object ApiValidationProfiles {

  val RAML_08_PROFILE: ValidationProfile = buildProfileFrom(Raml08Profile, Raml08Validations)
  val RAML_10_PROFILE: ValidationProfile = buildProfileFrom(Raml10Profile, Raml10Validations)

  val OAS_20_PROFILE: ValidationProfile = buildProfileFrom(Oas20Profile, Oas20Validations)
  val OAS_30_PROFILE: ValidationProfile = buildProfileFrom(Oas30Profile, Oas30Validations)

  val ASYNC_20_PROFILE: ValidationProfile = buildProfileFrom(Async20Profile, Async20Validations)
  val AMF_PROFILE: ValidationProfile      = buildProfileFrom(AmfProfile, AmfValidations)

  protected val apiProfiles: Map[ProfileName, ValidationProfile] = Map(
    RAML_08_PROFILE.name  -> RAML_08_PROFILE,
    RAML_10_PROFILE.name  -> RAML_10_PROFILE,
    OAS_20_PROFILE.name   -> OAS_20_PROFILE,
    OAS_30_PROFILE.name   -> OAS_30_PROFILE,
    ASYNC_20_PROFILE.name -> ASYNC_20_PROFILE,
    AMF_PROFILE.name      -> AMF_PROFILE
  )

  def profile(name: ProfileName): Option[ValidationProfile] = apiProfiles.get(name)
}
