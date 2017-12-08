package amf.validation

import amf.core.parser.Position.ZERO
import amf.core.{parser, validation}

import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.JSConverters._
import scala.scalajs.js


@JSExportAll
class AMFValidationResult(result: validation.AMFValidationResult) {
  val message: String = result.message
  val level: String = result.level
  val targetNode: String = result.targetNode
  val targetProperty: String = result.targetProperty.orNull
  val validationId: String = result.validationId
  val position: amf.core.parser.Range = result.position match {
    case Some(lexicalInformation) => lexicalInformation.range
    case _                        => new parser.Range(ZERO, ZERO)
  }
  val source = result.source
}

@JSExportAll
class AMFValidationReport(report: validation.AMFValidationReport) {
  val conforms: Boolean = report.conforms
  val model: String = report.model
  val profile: String = report.profile
  val results: js.Array[AMFValidationResult] = report.results.map(new AMFValidationResult(_)).toJSArray
}
