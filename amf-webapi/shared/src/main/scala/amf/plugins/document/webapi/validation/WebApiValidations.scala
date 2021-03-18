package amf.plugins.document.webapi.validation

import amf.core.validation.core.ValidationProfile

trait WebApiValidations {

  protected val defaultValidationProfiles: Map[String, () => ValidationProfile] =
    DefaultAMFValidations.profiles().foldLeft(Map[String, () => ValidationProfile]()) {
      case (acc, profile) =>
        acc.updated(profile.name.profile, { () =>
          profile
        })
    }
}
