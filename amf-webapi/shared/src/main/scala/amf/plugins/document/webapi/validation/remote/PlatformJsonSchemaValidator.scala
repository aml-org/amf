package amf.plugins.document.webapi.validation.remote

import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, ValidationCandidate}

import scala.concurrent.Future

trait PlatformJsonSchemaValidator {

  def validate(validationCandidates: Seq[ValidationCandidate], profile: ValidationProfile): Future[AMFValidationReport]

}
