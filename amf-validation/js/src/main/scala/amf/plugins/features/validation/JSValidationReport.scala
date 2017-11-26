package amf.plugins.features.validation

import amf.core.validation.core.{ValidationReport, ValidationResult}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class JSValidationReport(wrapped: js.Dynamic) extends ValidationReport {

  override def conforms: Boolean = wrapped.conforms().asInstanceOf[Boolean]

  override def results: List[ValidationResult] =
    wrapped.results().asInstanceOf[js.Array[js.Dynamic]].map(JSValidationResult.fromDynamic).toList

  override def toString: String = ValidationReport.displayString(this)
}
