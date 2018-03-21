package amf.client.validate

import amf.core.parser.Range
import amf.core.validation.{AMFValidationResult => InternalValidationResult}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class ValidationResult(private[amf] val _internal: InternalValidationResult) {

  val message: String        = _internal.message
  val level: String          = _internal.level
  val targetNode: String     = _internal.targetNode
  val targetProperty: String = _internal.targetProperty.orNull
  val validationId: String   = _internal.validationId
  val source: Any            = _internal.source

  val position: Range = _internal.position match {
    case Some(lexical) => lexical.range
    case _             => Range.NONE
  }
}
