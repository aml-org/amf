package amf.client.plugins

import amf.core.model.document.PayloadFragment
import amf.core.validation.AMFValidationResult

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
trait ValidationMode

@JSExportAll
@JSExportTopLevel("ValidationMode")
object ValidationMode {
  val StrictValidationMode: ValidationMode        = StrictValidationMode
  val ScalarRelaxedValidationMode: ValidationMode = ScalarRelaxedValidationMode
}

@JSExportAll
object StrictValidationMode extends ValidationMode
@JSExportAll
object ScalarRelaxedValidationMode extends ValidationMode
