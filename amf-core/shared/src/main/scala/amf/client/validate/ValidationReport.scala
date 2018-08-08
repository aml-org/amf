package amf.client.validate

import amf.ProfileName
import amf.client.convert.CoreClientConverters._
import amf.core.validation.{AMFValidationReport => InternalValidatorReport}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class ValidationReport(private[amf] val _internal: InternalValidatorReport) {

  @JSExportTopLevel("client.validate.ValidationReport")
  def this(conforms: Boolean, model: String, profile: ProfileName, results: ClientList[ValidationResult]) =
    this(InternalValidatorReport(conforms, model, profile, results.asInternal))

  def conforms: Boolean                     = _internal.conforms
  def model: String                         = _internal.model
  def profile: ProfileName                  = _internal.profile
  def results: ClientList[ValidationResult] = _internal.results.asClient

  override def toString: String = _internal.toString
}
